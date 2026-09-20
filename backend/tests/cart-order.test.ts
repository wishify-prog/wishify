import request from 'supertest';
import { createApp } from '../src/app';
import { prisma } from '../src/services/prisma.service';

jest.mock('../src/services/prisma.service', () => ({
  prisma: {
    banner: { findMany: jest.fn() },
    category: { findMany: jest.fn() },
    occasion: { findMany: jest.fn() },
    product: {
      findMany: jest.fn(),
      count: jest.fn(),
      findUnique: jest.fn(),
    },
    deliverySlot: {
      findMany: jest.fn(),
      findUnique: jest.fn(),
    },
    pincode: {
      findUnique: jest.fn(),
    },
    coupon: {
      findUnique: jest.fn(),
    },
  },
}));

const app = createApp();

describe('Catalog, Delivery & Coupon Flow', () => {
  beforeEach(() => {
    (prisma.banner.findMany as jest.Mock).mockResolvedValue([]);
    (prisma.category.findMany as jest.Mock).mockResolvedValue([]);
    (prisma.occasion.findMany as jest.Mock).mockResolvedValue([]);
    (prisma.product.findMany as jest.Mock).mockResolvedValue([]);
    (prisma.product.count as jest.Mock).mockResolvedValue(0);

    (prisma.deliverySlot.findMany as jest.Mock).mockResolvedValue([
      {
        id: 'slot-1',
        title: 'Standard (9 AM - 12 PM)',
        slotType: 'STANDARD',
        startTime: '09:00',
        endTime: '12:00',
        fee: 0,
        cutoffHoursBefore: 2,
        isActive: true,
      },
    ]);

    (prisma.pincode.findUnique as jest.Mock).mockResolvedValue({
      code: '560001',
      city: 'Bengaluru',
      state: 'Karnataka',
      isServiceable: true,
      isSameDayAvailable: true,
      isMidnightAvailable: true,
      standardDeliveryFee: 0,
      midnightDeliveryFee: 249,
    });
  });

  it('GET /api/v1/home should return curated feed with sections', async () => {
    const res = await request(app).get('/api/v1/home');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.sections).toHaveLength(4);
  });

  it('GET /api/v1/pincode/check/560001 should verify serviceable pincode', async () => {
    const res = await request(app).get('/api/v1/pincode/check/560001');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(res.body.data.isServiceable).toBe(true);
    expect(res.body.data.city).toBe('Bengaluru');
  });

  it('GET /api/v1/delivery-slots should return available slots', async () => {
    const res = await request(app).get('/api/v1/delivery-slots?pincode=560001');
    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    expect(Array.isArray(res.body.data)).toBe(true);
    expect(res.body.data.length).toBeGreaterThan(0);
  });

  it('POST /api/v1/coupons/validate should accurately calculate percentage discount', async () => {
    const mockCoupon = {
      id: 'coupon-1',
      code: 'FIRSTGIFT',
      discountType: 'PERCENTAGE',
      discountValue: 20,
      minOrderValue: 499,
      maxDiscountAmount: 200,
      validFrom: new Date('2025-01-01'),
      validTo: new Date('2028-01-01'),
      usedCount: 5,
      usageLimit: 1000,
      isActive: true,
    };

    (prisma.coupon.findUnique as jest.Mock).mockResolvedValue(mockCoupon);

    const res = await request(app)
      .post('/api/v1/coupons/validate')
      .send({ code: 'FIRSTGIFT', cartSubtotal: 800 });

    expect(res.status).toBe(200);
    expect(res.body.success).toBe(true);
    // 20% of 800 = 160
    expect(res.body.data.discountAmount).toBe(160);
    expect(res.body.data.finalAmount).toBe(640);
  });
});
