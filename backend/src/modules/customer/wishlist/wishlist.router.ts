import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';

export const customerWishlistRouter = Router();
customerWishlistRouter.use(authenticateJwt);

// GET /wishlist
customerWishlistRouter.get('/', async (req: Request, res: Response) => {
  try {
    const items = await prisma.wishlist.findMany({
      where: { userId: req.user!.id },
      include: {
        product: {
          include: {
            images: { where: { isPrimary: true }, take: 1 },
            variants: true,
          },
        },
      },
      orderBy: { createdAt: 'desc' },
    });
    return ApiResponse.success(res, items.map((w) => w.product));
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_WISHLIST');
  }
});

// POST /wishlist/:productId
customerWishlistRouter.post('/:productId', async (req: Request, res: Response) => {
  try {
    const { productId } = req.params;
    const item = await prisma.wishlist.upsert({
      where: {
        userId_productId: {
          userId: req.user!.id,
          productId,
        },
      },
      update: {},
      create: {
        userId: req.user!.id,
        productId,
      },
    });
    return ApiResponse.success(res, item, 201);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_ADD_TO_WISHLIST');
  }
});

// DELETE /wishlist/:productId
customerWishlistRouter.delete('/:productId', async (req: Request, res: Response) => {
  try {
    const { productId } = req.params;
    await prisma.wishlist.deleteMany({
      where: {
        userId: req.user!.id,
        productId,
      },
    });
    return ApiResponse.success(res, { message: 'Removed from wishlist' });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_REMOVE_FROM_WISHLIST');
  }
});
