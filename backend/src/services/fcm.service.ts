import * as admin from 'firebase-admin';
import { ENV } from '../config/env';
import { logger } from '../utils/logger';
import { prisma } from './prisma.service';

export class FcmService {
  private static isInitialized = false;

  private static init() {
    if (!this.isInitialized) {
      if (
        ENV.FIREBASE_PROJECT_ID &&
        ENV.FIREBASE_CLIENT_EMAIL &&
        ENV.FIREBASE_PRIVATE_KEY &&
        !ENV.FIREBASE_PRIVATE_KEY.includes('MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQC...')
      ) {
        try {
          admin.initializeApp({
            credential: admin.credential.cert({
              projectId: ENV.FIREBASE_PROJECT_ID,
              clientEmail: ENV.FIREBASE_CLIENT_EMAIL,
              privateKey: ENV.FIREBASE_PRIVATE_KEY,
            }),
          });
          this.isInitialized = true;
          logger.info('Firebase Admin SDK initialized successfully.');
        } catch (error) {
          logger.warn('Failed to initialize Firebase Admin SDK, falling back to mock mode:', error);
        }
      } else {
        logger.info('Firebase Admin SDK in MOCK mode (credentials unconfigured).');
      }
    }
  }

  static async sendNotification(
    userId: string,
    title: string,
    body: string,
    type: string = 'ORDER_UPDATE',
    metadata?: Record<string, any>
  ): Promise<void> {
    this.init();

    // 1. Always record in-app notification in DB
    try {
      await prisma.notification.create({
        data: {
          userId,
          title,
          body,
          type,
          metadata: metadata || {},
        },
      });
    } catch (err) {
      logger.error('Failed to create in-app notification in DB:', err);
    }

    // 2. Fetch device tokens
    const tokens = await prisma.deviceToken.findMany({
      where: { userId },
      select: { token: true },
    });

    if (tokens.length === 0) {
      logger.info(`No registered device tokens for user ${userId}. Push skipped.`);
      return;
    }

    const tokenList = tokens.map((t) => t.token);

    if (!this.isInitialized) {
      logger.info(`[MOCK PUSH] Sent push to user ${userId} [${tokenList.length} devices]: "${title}" - "${body}"`);
      return;
    }

    try {
      const response = await admin.messaging().sendEachForMulticast({
        tokens: tokenList,
        notification: { title, body },
        data: metadata ? Object.fromEntries(Object.entries(metadata).map(([k, v]) => [k, String(v)])) : {},
      });
      logger.info(`FCM push sent: ${response.successCount} success, ${response.failureCount} failure.`);
    } catch (error) {
      logger.error('Error sending FCM push:', error);
    }
  }

  static async notifyAdmins(title: string, body: string, metadata?: Record<string, any>): Promise<void> {
    const adminUsers = await prisma.user.findMany({
      where: { role: { in: ['ADMIN', 'SUPER_ADMIN'] } },
      select: { id: true },
    });

    for (const adminUser of adminUsers) {
      await this.sendNotification(adminUser.id, title, body, 'ADMIN_ALERT', metadata);
    }
  }
}
