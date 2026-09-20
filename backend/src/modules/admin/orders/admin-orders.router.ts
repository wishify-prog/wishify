import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role, OrderStatus, PaymentStatus } from '@prisma/client';
import { FcmService } from '../../../services/fcm.service';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';
import { eventBus } from '../../../services/event-bus.service';

const UpdateStatusSchema = z.object({
  body: z.object({
    status: z.nativeEnum(OrderStatus),
    notes: z.string().optional(),
  }),
});

const AssignDeliverySchema = z.object({
  body: z.object({
    deliveryPartnerName: z.string().min(2, 'Delivery partner name is required'),
    deliveryPartnerPhone: z.string().min(10, 'Delivery partner phone is required'),
    trackingNumber: z.string().optional(),
  }),
});

export const adminOrdersRouter = Router();

adminOrdersRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// GET /orders
adminOrdersRouter.get('/', async (req: Request, res: Response) => {
  try {
    const { status, paymentStatus, search, page, limit } = req.query;
    const pageNum = page ? Number(page) : 1;
    const limitNum = limit ? Number(limit) : 20;
    const skip = (pageNum - 1) * limitNum;

    const where: any = {};
    if (status) where.orderStatus = status as OrderStatus;
    if (paymentStatus) where.paymentStatus = paymentStatus as PaymentStatus;
    if (search) {
      where.OR = [
        { orderNumber: { contains: String(search), mode: 'insensitive' } },
        { recipientName: { contains: String(search), mode: 'insensitive' } },
        { recipientPhone: { contains: String(search), mode: 'insensitive' } },
      ];
    }

    const [total, orders] = await Promise.all([
      prisma.order.count({ where }),
      prisma.order.findMany({
        where,
        include: {
          items: true,
          deliverySlot: true,
          user: { select: { id: true, name: true, phone: true } },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limitNum,
      }),
    ]);

    return ApiResponse.paginated(res, orders, total, pageNum, limitNum);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_ORDERS');
  }
});

// GET /orders/:id
adminOrdersRouter.get('/:id', async (req: Request, res: Response) => {
  try {
    const order = await prisma.order.findUnique({
      where: { id: req.params.id },
      include: {
        items: { include: { addOns: true } },
        address: true,
        deliverySlot: true,
        payments: true,
        statusHistory: {
          include: { updatedByUser: { select: { id: true, name: true, role: true } } },
          orderBy: { createdAt: 'asc' },
        },
        user: { select: { id: true, name: true, phone: true, email: true } },
      },
    });

    if (!order) {
      return ApiResponse.error(res, 'Order not found', 404, 'ORDER_NOT_FOUND');
    }

    return ApiResponse.success(res, order);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_ORDER');
  }
});

// PATCH /orders/:id/status
adminOrdersRouter.patch('/:id/status', validate(UpdateStatusSchema), async (req: Request, res: Response) => {
  try {
    const { status, notes } = req.body;
    const orderId = req.params.id;

    const existingOrder = await prisma.order.findUnique({
      where: { id: orderId },
      include: { user: true },
    });

    if (!existingOrder) {
      return ApiResponse.error(res, 'Order not found', 404, 'ORDER_NOT_FOUND');
    }

    // Update order status & add history in transaction
    const updatedOrder = await prisma.$transaction(async (tx) => {
      const order = await tx.order.update({
        where: { id: orderId },
        data: {
          orderStatus: status,
          ...(status === OrderStatus.DELIVERED && {
            paymentStatus: existingOrder.paymentMethod === 'COD' ? PaymentStatus.PAID : existingOrder.paymentStatus,
          }),
        },
      });

      await tx.orderStatusHistory.create({
        data: {
          orderId,
          status,
          notes: notes || `Status updated to ${status} by admin`,
          updatedByUserId: req.user!.id,
        },
      });

      return order;
    });

    // Notify Customer via FCM Push
    const statusMessages: Record<OrderStatus, { title: string; body: string }> = {
      PLACED: { title: 'Order Received', body: `Your order ${existingOrder.orderNumber} is placed.` },
      CONFIRMED: { title: 'Order Confirmed! 💐', body: `Your order ${existingOrder.orderNumber} has been accepted and confirmed.` },
      PREPARED: { title: 'Order Prepared 🎂', body: `Your gift for ${existingOrder.recipientName} is freshly prepared & packaged with care!` },
      OUT_FOR_DELIVERY: { title: 'Out for Delivery 🚚', body: `Our delivery partner is on the way with your gift!` },
      DELIVERED: { title: 'Gift Delivered! 🎁✨', body: `Your gift has been delivered to ${existingOrder.recipientName}. Thank you for choosing Wishify!` },
      CANCELLED: { title: 'Order Cancelled', body: `Your order ${existingOrder.orderNumber} has been cancelled.` },
    };

    const pushContent = statusMessages[status as OrderStatus];
    if (pushContent) {
      await FcmService.sendNotification(
        existingOrder.userId,
        pushContent.title,
        pushContent.body,
        'ORDER_UPDATE',
        { orderId: existingOrder.id, status }
      );
    }

    // Broadcast real-time event to mobile apps
    eventBus.broadcast('ORDER_UPDATED', {
      orderId: existingOrder.id,
      userId: existingOrder.userId,
      status,
    });

    return ApiResponse.success(res, updatedOrder);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_UPDATE_STATUS');
  }
});

// POST /orders/:id/assign
adminOrdersRouter.post('/:id/assign', validate(AssignDeliverySchema), async (req: Request, res: Response) => {
  try {
    const { deliveryPartnerName, deliveryPartnerPhone, trackingNumber } = req.body;
    const order = await prisma.order.update({
      where: { id: req.params.id },
      data: {
        deliveryPartnerName,
        deliveryPartnerPhone,
        trackingNumber: trackingNumber || `TRK-${Date.now().toString().slice(-6)}`,
      },
    });

    eventBus.broadcast('ORDER_UPDATED', {
      orderId: order.id,
      userId: order.userId,
      deliveryPartnerName,
      trackingNumber: order.trackingNumber,
    });

    return ApiResponse.success(res, order);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_ASSIGN_DELIVERY');
  }
});

// POST /orders/:id/refund
adminOrdersRouter.post('/:id/refund', async (req: Request, res: Response) => {
  try {
    const orderId = req.params.id;
    const order = await prisma.order.findUnique({
      where: { id: orderId },
      include: { payments: true },
    });

    if (!order) {
      return ApiResponse.error(res, 'Order not found', 404, 'ORDER_NOT_FOUND');
    }

    const updated = await prisma.$transaction([
      prisma.order.update({
        where: { id: orderId },
        data: { paymentStatus: PaymentStatus.REFUNDED },
      }),
      prisma.payment.updateMany({
        where: { orderId },
        data: { status: PaymentStatus.REFUNDED },
      }),
      prisma.orderStatusHistory.create({
        data: {
          orderId,
          status: order.orderStatus,
          notes: `Refund issued for order amount ₹${order.totalAmount}`,
          updatedByUserId: req.user!.id,
        },
      }),
    ]);

    // Send push notification to user
    await FcmService.sendNotification(
      order.userId,
      '💸 Refund Processed',
      `A refund of ₹${order.totalAmount} has been processed for order ${order.orderNumber}.`,
      'PAYMENT_REFUND',
      { orderId }
    );

    eventBus.broadcast('ORDER_UPDATED', {
      orderId,
      userId: order.userId,
      paymentStatus: PaymentStatus.REFUNDED,
    });

    return ApiResponse.success(res, { message: 'Refund processed successfully', orderId });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'REFUND_FAILED');
  }
});
