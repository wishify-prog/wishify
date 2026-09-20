import { prisma } from '../../../services/prisma.service';
import { CONSTANTS } from '../../../config/constants';
import { OrderStatus, PaymentStatus } from '@prisma/client';
import { FcmService } from '../../../services/fcm.service';

export class CustomerOrderService {
  static async listOrders(userId: string, page: number = 1, limit: number = CONSTANTS.DEFAULT_PAGE_SIZE) {
    const skip = (page - 1) * limit;

    const [total, orders] = await Promise.all([
      prisma.order.count({ where: { userId } }),
      prisma.order.findMany({
        where: { userId },
        include: {
          items: {
            include: {
              addOns: true,
            },
          },
          deliverySlot: true,
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
    ]);

    return { total, page, limit, orders };
  }

  static async getOrderDetail(userId: string, orderId: string) {
    const order = await prisma.order.findFirst({
      where: { id: orderId, userId },
      include: {
        items: {
          include: {
            addOns: true,
          },
        },
        address: true,
        deliverySlot: true,
        statusHistory: {
          orderBy: { createdAt: 'asc' },
        },
        payments: true,
      },
    });

    if (!order) {
      throw new Error('ORDER_NOT_FOUND');
    }

    return order;
  }

  static async cancelOrder(userId: string, orderId: string, reason?: string) {
    const order = await prisma.order.findFirst({
      where: { id: orderId, userId },
      include: { items: true },
    });

    if (!order) {
      throw new Error('ORDER_NOT_FOUND');
    }

    if (order.orderStatus !== OrderStatus.PLACED && order.orderStatus !== OrderStatus.CONFIRMED) {
      throw new Error(`Order cannot be cancelled in status '${order.orderStatus}'`);
    }

    // Restore stock in transaction
    const updated = await prisma.$transaction(async (tx) => {
      const cancelledOrder = await tx.order.update({
        where: { id: orderId },
        data: {
          orderStatus: OrderStatus.CANCELLED,
          cancelReason: reason || 'Cancelled by customer',
        },
      });

      // Restore stock for variants
      for (const item of order.items) {
        if (item.variantId) {
          await tx.productVariant.update({
            where: { id: item.variantId },
            data: { stockQuantity: { increment: item.quantity } },
          });
        }
      }

      await tx.orderStatusHistory.create({
        data: {
          orderId: order.id,
          status: OrderStatus.CANCELLED,
          notes: reason ? `Customer cancelled: ${reason}` : 'Cancelled by customer',
        },
      });

      return cancelledOrder;
    });

    // Notify Admins
    await FcmService.notifyAdmins(
      '⚠️ Order Cancelled',
      `Order ${order.orderNumber} was cancelled by customer.`,
      { orderId: order.id }
    );

    return updated;
  }

  static async getInvoice(userId: string, orderId: string) {
    const order = await prisma.order.findFirst({
      where: { id: orderId, userId },
      include: {
        items: { include: { addOns: true } },
        address: true,
        deliverySlot: true,
        payments: true,
        user: { select: { id: true, name: true, phone: true, email: true } },
      },
    });

    if (!order) {
      throw new Error('ORDER_NOT_FOUND');
    }

    return {
      invoiceNumber: `INV-${order.orderNumber.replace('WISH-', '')}`,
      invoiceDate: order.createdAt,
      orderNumber: order.orderNumber,
      customer: order.user,
      shippingAddress: order.address,
      recipient: {
        name: order.recipientName,
        phone: order.recipientPhone,
        isSenderHidden: order.isSenderHidden,
      },
      delivery: {
        date: order.deliveryDate,
        slot: order.deliverySlot.title,
      },
      items: order.items,
      pricing: {
        subtotal: order.subtotal,
        deliveryFee: order.deliveryFee,
        discountAmount: order.discountAmount,
        couponCode: order.couponCode,
        taxAmount: order.taxAmount,
        totalAmount: order.totalAmount,
      },
      payment: {
        method: order.paymentMethod,
        status: order.paymentStatus,
        transactionRef: order.payments[0]?.transactionReference || null,
      },
    };
  }
}
