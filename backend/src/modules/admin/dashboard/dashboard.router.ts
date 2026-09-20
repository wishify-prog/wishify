import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role, OrderStatus } from '@prisma/client';

export const adminDashboardRouter = Router();

adminDashboardRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// GET /dashboard/stats
adminDashboardRouter.get('/stats', async (req: Request, res: Response) => {
  try {
    const today = new Date();
    today.setHours(0, 0, 0, 0);

    const [
      totalOrders,
      todayOrders,
      revenueResult,
      todayRevenueResult,
      pendingOrders,
      lowStockVariants,
      topProducts,
    ] = await Promise.all([
      prisma.order.count(),
      prisma.order.count({ where: { createdAt: { gte: today } } }),
      prisma.order.aggregate({
        where: { paymentStatus: 'PAID' },
        _sum: { totalAmount: true },
      }),
      prisma.order.aggregate({
        where: { paymentStatus: 'PAID', createdAt: { gte: today } },
        _sum: { totalAmount: true },
      }),
      prisma.order.count({
        where: {
          orderStatus: { in: [OrderStatus.PLACED, OrderStatus.CONFIRMED, OrderStatus.PREPARED, OrderStatus.OUT_FOR_DELIVERY] },
        },
      }),
      prisma.productVariant.count({
        where: { stockQuantity: { lte: 15 } },
      }),
      prisma.orderItem.groupBy({
        by: ['productId', 'title'],
        _sum: { quantity: true, totalPrice: true },
        orderBy: { _sum: { quantity: 'desc' } },
        take: 5,
      }),
    ]);

    return ApiResponse.success(res, {
      totalOrders,
      todayOrders,
      totalRevenue: revenueResult._sum.totalAmount || 0,
      todayRevenue: todayRevenueResult._sum.totalAmount || 0,
      pendingOrders,
      lowStockCount: lowStockVariants,
      topSellingProducts: topProducts.map((p) => ({
        productId: p.productId,
        title: p.title,
        totalQuantitySold: p._sum.quantity || 0,
        totalRevenue: p._sum.totalPrice || 0,
      })),
    });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_DASHBOARD_STATS');
  }
});

// GET /dashboard/charts
adminDashboardRouter.get('/charts', async (req: Request, res: Response) => {
  try {
    const thirtyDaysAgo = new Date();
    thirtyDaysAgo.setDate(thirtyDaysAgo.getDate() - 30);

    const orders = await prisma.order.findMany({
      where: {
        createdAt: { gte: thirtyDaysAgo },
        paymentStatus: 'PAID',
      },
      select: {
        createdAt: true,
        totalAmount: true,
      },
      orderBy: { createdAt: 'asc' },
    });

    // Group by date YYYY-MM-DD
    const chartMap = new Map<string, { date: string; revenue: number; orderCount: number }>();

    for (let i = 29; i >= 0; i--) {
      const d = new Date();
      d.setDate(d.getDate() - i);
      const key = d.toISOString().split('T')[0];
      chartMap.set(key, { date: key, revenue: 0, orderCount: 0 });
    }

    for (const ord of orders) {
      const key = ord.createdAt.toISOString().split('T')[0];
      if (chartMap.has(key)) {
        const curr = chartMap.get(key)!;
        curr.revenue += ord.totalAmount;
        curr.orderCount += 1;
      }
    }

    return ApiResponse.success(res, Array.from(chartMap.values()));
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_CHARTS');
  }
});
