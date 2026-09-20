import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';

const DeviceTokenSchema = z.object({
  body: z.object({
    token: z.string().min(10, 'FCM token is required'),
    platform: z.enum(['ANDROID', 'IOS', 'WEB']).default('ANDROID'),
  }),
});

export const customerNotificationRouter = Router();
customerNotificationRouter.use(authenticateJwt);

// GET /notifications
customerNotificationRouter.get('/', async (req: Request, res: Response) => {
  try {
    const notifications = await prisma.notification.findMany({
      where: { userId: req.user!.id },
      orderBy: { createdAt: 'desc' },
      take: 50,
    });

    // Mark unread as read
    await prisma.notification.updateMany({
      where: { userId: req.user!.id, isRead: false },
      data: { isRead: true },
    });

    return ApiResponse.success(res, notifications);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_NOTIFICATIONS');
  }
});

// POST /notifications/token
customerNotificationRouter.post('/token', validate(DeviceTokenSchema), async (req: Request, res: Response) => {
  try {
    const { token, platform } = req.body;
    const deviceToken = await prisma.deviceToken.upsert({
      where: { token },
      update: { userId: req.user!.id, platform },
      create: {
        userId: req.user!.id,
        token,
        platform,
      },
    });
    return ApiResponse.success(res, deviceToken);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_REGISTER_DEVICE_TOKEN');
  }
});
