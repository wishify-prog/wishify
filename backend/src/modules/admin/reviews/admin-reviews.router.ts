import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role, ReviewStatus } from '@prisma/client';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';

const UpdateReviewSchema = z.object({
  body: z.object({
    status: z.nativeEnum(ReviewStatus),
  }),
});

export const adminReviewsRouter = Router();

adminReviewsRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// GET /reviews
adminReviewsRouter.get('/', async (req: Request, res: Response) => {
  try {
    const status = req.query.status as ReviewStatus | undefined;
    const page = req.query.page ? Number(req.query.page) : 1;
    const limit = req.query.limit ? Number(req.query.limit) : 20;
    const skip = (page - 1) * limit;

    const where = status ? { status } : {};

    const [total, reviews] = await Promise.all([
      prisma.review.count({ where }),
      prisma.review.findMany({
        where,
        include: {
          product: { select: { id: true, title: true, slug: true } },
          user: { select: { id: true, name: true, phone: true } },
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limit,
      }),
    ]);

    return ApiResponse.paginated(res, reviews, total, page, limit);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_REVIEWS');
  }
});

// PATCH /reviews/:id
adminReviewsRouter.patch('/:id', validate(UpdateReviewSchema), async (req: Request, res: Response) => {
  try {
    const { status } = req.body;
    const review = await prisma.review.update({
      where: { id: req.params.id },
      data: { status },
      include: { product: true },
    });

    // If approved, update product average rating
    if (status === ReviewStatus.APPROVED) {
      const stats = await prisma.review.aggregate({
        where: { productId: review.productId, status: ReviewStatus.APPROVED },
        _avg: { rating: true },
        _count: { rating: true },
      });

      await prisma.product.update({
        where: { id: review.productId },
        data: {
          averageRating: stats._avg.rating || 5.0,
          reviewCount: stats._count.rating || 0,
        },
      });
    }

    return ApiResponse.success(res, review);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_UPDATE_REVIEW');
  }
});
