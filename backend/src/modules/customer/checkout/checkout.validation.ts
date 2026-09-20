import { z } from 'zod';
import { PaymentMethod } from '@prisma/client';

export const InitiateCheckoutSchema = z.object({
  body: z.object({
    addressId: z.string().uuid('Invalid address ID'),
    couponCode: z.string().optional(),
    paymentMethod: z.nativeEnum(PaymentMethod).default(PaymentMethod.RAZORPAY),
    recipientName: z.string().min(2, 'Recipient name is required'),
    recipientPhone: z.string().min(10, 'Recipient phone must be at least 10 digits'),
    giftMessage: z.string().max(300).optional(),
    isSenderHidden: z.boolean().default(false),
    deliveryDate: z.string().min(10, 'Delivery date is required (YYYY-MM-DD)'),
    deliverySlotId: z.string().uuid('Delivery slot is required'),
  }),
});

export const VerifyPaymentSchema = z.object({
  body: z.object({
    orderId: z.string().uuid('Invalid order ID'),
    razorpayOrderId: z.string().min(1, 'Razorpay order ID is required'),
    razorpayPaymentId: z.string().min(1, 'Razorpay payment ID is required'),
    razorpaySignature: z.string().min(1, 'Razorpay signature is required'),
  }),
});
