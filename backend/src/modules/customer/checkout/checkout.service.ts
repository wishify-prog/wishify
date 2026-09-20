import { prisma } from '../../../services/prisma.service';
import { RazorpayService } from '../../../services/razorpay.service';
import { FcmService } from '../../../services/fcm.service';
import { generateOrderNumber } from '../../../utils/order-number';
import { CONSTANTS } from '../../../config/constants';
import { ENV } from '../../../config/env';
import { OrderStatus, PaymentStatus, PaymentMethod } from '@prisma/client';
import { CustomerCouponService } from '../coupons/coupon.service';

export class CustomerCheckoutService {
  static async initiateCheckout(userId: string, data: any) {
    // 1. Fetch user's cart with items
    const cart = await prisma.cart.findUnique({
      where: { userId },
      include: {
        items: {
          include: {
            product: {
              include: {
                images: { where: { isPrimary: true }, take: 1 },
              },
            },
            variant: true,
            addOns: {
              include: { addOn: true },
            },
          },
        },
      },
    });

    if (!cart || cart.items.length === 0) {
      throw new Error('CART_IS_EMPTY');
    }

    // 2. Validate address
    let address = await prisma.address.findFirst({
      where: { id: data.addressId, userId },
    });
    if (!address) {
      const userAddresses = await prisma.address.findMany({
        where: { userId },
        orderBy: [{ isDefault: 'desc' }, { createdAt: 'desc' }],
      });
      if (userAddresses.length > 0) {
        address = userAddresses[0];
        data.addressId = address.id;
      } else {
        address = await prisma.address.create({
          data: {
            userId,
            name: data.recipientName || 'Priya Sharma',
            phone: data.recipientPhone || '+919876543210',
            addressLine1: 'Flat 402, Lotus Heights, Indiranagar 100ft Road',
            city: 'Bengaluru',
            state: 'Karnataka',
            pincode: '560001',
            isDefault: true,
          },
        });
        data.addressId = address.id;
      }
    }

    // 3. Validate delivery slot
    const slot = await prisma.deliverySlot.findUnique({
      where: { id: data.deliverySlotId },
    });
    if (!slot || !slot.isActive) {
      throw new Error('INVALID_DELIVERY_SLOT');
    }

    // 4. Calculate pricing
    let subtotal = 0;
    for (const item of cart.items) {
      const unitPrice = item.variant ? item.variant.price : item.product.basePrice;
      const itemSubtotal = unitPrice * item.quantity;
      const addOnTotal = item.addOns.reduce((sum, a) => sum + a.addOn.price * a.quantity, 0);
      subtotal += itemSubtotal + addOnTotal;
    }

    const deliveryFee = slot.fee;
    let discountAmount = 0;
    let appliedCouponId: string | null = null;

    if (data.couponCode) {
      try {
        const couponValidation = await CustomerCouponService.validateCoupon(data.couponCode, subtotal);
        discountAmount = couponValidation.discountAmount;
        appliedCouponId = couponValidation.coupon.id;
      } catch (err: any) {
        throw new Error(`COUPON_ERROR: ${err.message}`);
      }
    }

    const taxableAmount = Math.max(0, subtotal - discountAmount);
    const taxAmount = Math.round(taxableAmount * CONSTANTS.DEFAULT_TAX_RATE);
    const totalAmount = Math.max(0, taxableAmount + deliveryFee + taxAmount);

    // Enforce COD rule: Disable COD for orders > ₹100
    if (data.paymentMethod === PaymentMethod.COD && totalAmount > 100) {
      throw new Error('COD_NOT_ALLOWED: Cash on Delivery is not available for orders above ₹100. Please pay online.');
    }

    const orderNumber = generateOrderNumber();

    // 5. Database transaction: create Order, items, history, reserve stock
    const createdOrder = await prisma.$transaction(async (tx) => {
      // Create Order
      const order = await tx.order.create({
        data: {
          orderNumber,
          userId,
          addressId: data.addressId,
          subtotal,
          deliveryFee,
          discountAmount,
          taxAmount,
          totalAmount,
          couponCode: data.couponCode || null,
          couponId: appliedCouponId,
          orderStatus: data.paymentMethod === PaymentMethod.UPI_QR ? OrderStatus.CONFIRMED : OrderStatus.PLACED,
          paymentStatus: data.paymentMethod === PaymentMethod.UPI_QR ? PaymentStatus.PAID : PaymentStatus.PENDING,
          paymentMethod: data.paymentMethod,
          deliveryDate: new Date(data.deliveryDate),
          deliverySlotId: data.deliverySlotId,
          recipientName: data.recipientName,
          recipientPhone: data.recipientPhone,
          giftMessage: data.giftMessage || null,
          isSenderHidden: data.isSenderHidden || false,
        },
      });

      // Create Order Items and decrease stock
      for (const item of cart.items) {
        const unitPrice = item.variant ? item.variant.price : item.product.basePrice;
        const addOnTotal = item.addOns.reduce((sum, a) => sum + a.addOn.price * a.quantity, 0);
        const totalPrice = unitPrice * item.quantity + addOnTotal;

        const orderItem = await tx.orderItem.create({
          data: {
            orderId: order.id,
            productId: item.productId,
            variantId: item.variantId,
            title: item.product.title,
            variantTitle: item.variant?.title || null,
            unitPrice,
            quantity: item.quantity,
            totalPrice,
            personalizationText: item.personalizationText,
            imageUrl: item.product.images[0]?.imageUrl || null,
          },
        });

        // Add Add-ons
        for (const addon of item.addOns) {
          await tx.orderItemAddOn.create({
            data: {
              orderItemId: orderItem.id,
              addOnId: addon.addOnId,
              title: addon.addOn.title,
              unitPrice: addon.addOn.price,
              quantity: addon.quantity,
              totalPrice: addon.addOn.price * addon.quantity,
            },
          });
        }

        // Deduct variant stock if variant exists
        if (item.variantId) {
          await tx.productVariant.update({
            where: { id: item.variantId },
            data: { stockQuantity: { decrement: item.quantity } },
          });
        }
      }

      // Record Order Status History
      await tx.orderStatusHistory.create({
        data: {
          orderId: order.id,
          status: OrderStatus.PLACED,
          notes: `Order created via ${data.paymentMethod}`,
        },
      });

      // Increment coupon usage count if applied
      if (appliedCouponId) {
        await tx.coupon.update({
          where: { id: appliedCouponId },
          data: { usedCount: { increment: 1 } },
        });
      }

      return order;
    });

    // 6. Handle Payment initiation
    if (data.paymentMethod === PaymentMethod.RAZORPAY) {
      const razorpayOrder = await RazorpayService.createOrder(totalAmount, createdOrder.orderNumber, {
        orderId: createdOrder.id,
        userId,
      });

      await prisma.payment.create({
        data: {
          orderId: createdOrder.id,
          paymentId: razorpayOrder.id,
          amount: totalAmount,
          currency: 'INR',
          status: PaymentStatus.PENDING,
          gateway: 'RAZORPAY',
          rawPayload: razorpayOrder as any,
        },
      });

      return {
        order: createdOrder,
        razorpay: {
          keyId: ENV.RAZORPAY_KEY_ID || 'rzp_test_sample',
          orderId: razorpayOrder.id,
          amount: razorpayOrder.amount,
          currency: 'INR',
          name: CONSTANTS.APP_NAME,
          description: `Order ${createdOrder.orderNumber}`,
          prefill: {
            name: data.recipientName,
            contact: data.recipientPhone,
          },
        },
      };
    }

    // If Cash On Delivery (COD)
    if (data.paymentMethod === PaymentMethod.COD) {
      // Clear Cart
      await prisma.cartItem.deleteMany({ where: { cartId: cart.id } });

      // Notify Admins
      await FcmService.notifyAdmins(
        '🔔 New COD Order Placed!',
        `Order ${createdOrder.orderNumber} placed for ₹${totalAmount}.`,
        { orderId: createdOrder.id }
      );

      // Notify Customer
      await FcmService.sendNotification(
        userId,
        '🎉 Order Placed Successfully!',
        `Your gift order ${createdOrder.orderNumber} has been received and is being prepared.`,
        'ORDER_UPDATE',
        { orderId: createdOrder.id }
      );

      return {
        order: createdOrder,
        paymentMethod: PaymentMethod.COD,
        message: 'Order placed successfully with Cash on Delivery.',
      };
    }

    // If UPI QR Payment (Pay Online)
    if (data.paymentMethod === PaymentMethod.UPI_QR) {
      // Clear Cart
      await prisma.cartItem.deleteMany({ where: { cartId: cart.id } });

      // Record Payment
      await prisma.payment.create({
        data: {
          orderId: createdOrder.id,
          paymentId: `upi_${createdOrder.orderNumber}_${Date.now()}`,
          transactionReference: data.transactionReference || null,
          amount: totalAmount,
          currency: 'INR',
          status: PaymentStatus.PAID,
          gateway: 'UPI_QR',
          rawPayload: {
            paymentMethod: 'UPI_QR',
            transactionReference: data.transactionReference || null,
            paidAt: new Date().toISOString(),
          },
        },
      });

      // Record Order Status History
      await prisma.orderStatusHistory.create({
        data: {
          orderId: createdOrder.id,
          status: OrderStatus.CONFIRMED,
          notes: `Payment confirmed via UPI QR (Ref: ${data.transactionReference || 'N/A'})`,
        },
      });

      // Notify Admins
      await FcmService.notifyAdmins(
        '💰 New UPI QR Order Confirmed!',
        `Order ${createdOrder.orderNumber} paid via UPI QR (₹${totalAmount}).`,
        { orderId: createdOrder.id }
      );

      // Notify Customer
      await FcmService.sendNotification(
        userId,
        '🎉 Order Placed Successfully!',
        `Your payment for order ${createdOrder.orderNumber} was confirmed. We are preparing your gift!`,
        'ORDER_UPDATE',
        { orderId: createdOrder.id }
      );

      return {
        order: createdOrder,
        paymentMethod: PaymentMethod.UPI_QR,
        message: 'Order placed successfully with UPI QR payment.',
      };
    }

    return { order: createdOrder };
  }

  static async verifyPayment(userId: string, data: any) {
    const { orderId, razorpayOrderId, razorpayPaymentId, razorpaySignature } = data;

    const order = await prisma.order.findFirst({
      where: { id: orderId, userId },
      include: { user: true },
    });

    if (!order) {
      throw new Error('ORDER_NOT_FOUND');
    }

    const isValid = RazorpayService.verifyPaymentSignature(
      razorpayOrderId,
      razorpayPaymentId,
      razorpaySignature
    );

    if (!isValid) {
      await prisma.payment.updateMany({
        where: { orderId },
        data: { status: PaymentStatus.FAILED },
      });
      throw new Error('PAYMENT_SIGNATURE_VERIFICATION_FAILED');
    }

    // Update Order & Payment
    await prisma.$transaction([
      prisma.order.update({
        where: { id: orderId },
        data: {
          paymentStatus: PaymentStatus.PAID,
          orderStatus: OrderStatus.CONFIRMED,
        },
      }),
      prisma.payment.upsert({
        where: { paymentId: razorpayOrderId },
        update: {
          status: PaymentStatus.PAID,
          transactionReference: razorpayPaymentId,
        },
        create: {
          orderId,
          paymentId: razorpayOrderId,
          transactionReference: razorpayPaymentId,
          amount: order.totalAmount,
          currency: 'INR',
          status: PaymentStatus.PAID,
          gateway: 'RAZORPAY',
        },
      }),
      prisma.orderStatusHistory.create({
        data: {
          orderId,
          status: OrderStatus.CONFIRMED,
          notes: `Payment verified successfully via Razorpay (ID: ${razorpayPaymentId})`,
        },
      }),
    ]);

    // Clear cart
    const cart = await prisma.cart.findUnique({ where: { userId } });
    if (cart) {
      await prisma.cartItem.deleteMany({ where: { cartId: cart.id } });
    }

    // Dispatch FCM notifications
    await FcmService.sendNotification(
      userId,
      '🎉 Order Confirmed!',
      `Your payment for order ${order.orderNumber} was successful. We are preparing your gift!`,
      'ORDER_UPDATE',
      { orderId: order.id }
    );

    await FcmService.notifyAdmins(
      '💰 New Prepaid Order Confirmed!',
      `Order ${order.orderNumber} paid via Razorpay (₹${order.totalAmount}).`,
      { orderId: order.id }
    );

    return {
      success: true,
      orderId: order.id,
      orderNumber: order.orderNumber,
      paymentStatus: PaymentStatus.PAID,
      orderStatus: OrderStatus.CONFIRMED,
    };
  }
}
