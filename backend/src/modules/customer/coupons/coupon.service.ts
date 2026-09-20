import { prisma } from '../../../services/prisma.service';

export class CustomerCouponService {
  static async listCoupons() {
    const now = new Date();
    return prisma.coupon.findMany({
      where: {
        isActive: true,
        validFrom: { lte: now },
        validTo: { gte: now },
      },
      select: {
        id: true,
        code: true,
        description: true,
        discountType: true,
        discountValue: true,
        minOrderValue: true,
        maxDiscountAmount: true,
        validTo: true,
      },
      orderBy: { discountValue: 'desc' },
    });
  }

  static async validateCoupon(code: string, cartSubtotal: number) {
    const now = new Date();
    const coupon = await prisma.coupon.findUnique({
      where: { code: code.toUpperCase().trim() },
    });

    if (!coupon || !coupon.isActive || coupon.validFrom > now || coupon.validTo < now) {
      throw new Error('COUPON_INVALID_OR_EXPIRED');
    }

    if (coupon.usedCount >= coupon.usageLimit) {
      throw new Error('COUPON_USAGE_LIMIT_REACHED');
    }

    if (cartSubtotal < coupon.minOrderValue) {
      throw new Error(`Minimum order value of ₹${coupon.minOrderValue} required for this coupon`);
    }

    let discountAmount = 0;
    if (coupon.discountType === 'PERCENTAGE') {
      discountAmount = (cartSubtotal * coupon.discountValue) / 100;
      if (coupon.maxDiscountAmount && discountAmount > coupon.maxDiscountAmount) {
        discountAmount = coupon.maxDiscountAmount;
      }
    } else {
      discountAmount = Math.min(coupon.discountValue, cartSubtotal);
    }

    return {
      isValid: true,
      coupon: {
        id: coupon.id,
        code: coupon.code,
        description: coupon.description,
        discountType: coupon.discountType,
        discountValue: coupon.discountValue,
      },
      discountAmount: Math.round(discountAmount),
      finalAmount: Math.max(0, Math.round(cartSubtotal - discountAmount)),
    };
  }
}
