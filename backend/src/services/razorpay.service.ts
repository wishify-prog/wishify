import Razorpay from 'razorpay';
import crypto from 'crypto';
import { ENV } from '../config/env';
import { logger } from '../utils/logger';

export class RazorpayService {
  private static instance: Razorpay | null = null;
  private static isMockMode: boolean = false;

  private static getClient(): Razorpay | null {
    if (!this.instance) {
      if (
        !ENV.RAZORPAY_KEY_ID ||
        !ENV.RAZORPAY_KEY_SECRET ||
        ENV.RAZORPAY_KEY_ID.includes('sample')
      ) {
        logger.info('Razorpay initialized in MOCK mode (sandbox fallback).');
        this.isMockMode = true;
        return null;
      }

      this.instance = new Razorpay({
        key_id: ENV.RAZORPAY_KEY_ID,
        key_secret: ENV.RAZORPAY_KEY_SECRET,
      });
      this.isMockMode = false;
    }
    return this.instance;
  }

  static async createOrder(amount: number, receipt: string, notes?: Record<string, any>) {
    const client = this.getClient();
    const amountInPaise = Math.round(amount * 100);

    if (this.isMockMode || !client) {
      const mockOrderId = `order_mock_${Date.now()}_${Math.random().toString(36).substring(2, 7)}`;
      logger.info(`[MOCK RAZORPAY] Created order ${mockOrderId} for ₹${amount}`);
      return {
        id: mockOrderId,
        entity: 'order',
        amount: amountInPaise,
        amount_paid: 0,
        amount_due: amountInPaise,
        currency: 'INR',
        receipt,
        status: 'created',
        notes: notes || {},
      };
    }

    try {
      const order = await client.orders.create({
        amount: amountInPaise,
        currency: 'INR',
        receipt,
        notes: notes || {},
      });
      return order;
    } catch (error) {
      logger.error('Failed to create Razorpay order:', error);
      throw error;
    }
  }

  static verifyPaymentSignature(
    razorpayOrderId: string,
    razorpayPaymentId: string,
    signature: string
  ): boolean {
    if (this.isMockMode || razorpayOrderId.startsWith('order_mock_')) {
      logger.info(`[MOCK RAZORPAY] Auto-verifying signature for ${razorpayOrderId}`);
      return true;
    }

    try {
      const body = `${razorpayOrderId}|${razorpayPaymentId}`;
      const expectedSignature = crypto
        .createHmac('sha256', ENV.RAZORPAY_KEY_SECRET)
        .update(body)
        .digest('hex');

      return expectedSignature === signature;
    } catch (error) {
      logger.error('Error verifying Razorpay signature:', error);
      return false;
    }
  }
}
