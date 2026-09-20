import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';

export const adminCustomersRouter = Router();

adminCustomersRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// GET /customers
adminCustomersRouter.get('/', async (req: Request, res: Response) => {
  try {
    const page = req.query.page ? Number(req.query.page) : 1;
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const skip = (page - 1) * limit;

    const [total, customers] = await Promise.all([
      prisma.user.count({ where: { role: Role.CUSTOMER } }),
      prisma.user.findMany({
        where: { role: Role.CUSTOMER },
        select: {
          id: true,
          name: true,
          phone: true,
          email: true,
          walletBalance: true,
          createdAt: true,
          _count: {
            select: { orders: true },
          },
          orders: {
            where: { paymentStatus: 'PAID' },
            select: { totalAmount: true },
          },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
    ]);

    const formatted = customers.map((c) => ({
      id: c.id,
      name: c.name,
      phone: c.phone,
      email: c.email,
      walletBalance: c.walletBalance,
      createdAt: c.createdAt,
      orderCount: c._count.orders,
      totalSpend: c.orders.reduce((sum, o) => sum + o.totalAmount, 0),
    }));

    return ApiResponse.paginated(res, formatted, total, page, limit);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_CUSTOMERS');
  }
});
