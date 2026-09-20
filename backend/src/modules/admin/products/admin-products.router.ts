import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';
import { eventBus } from '../../../services/event-bus.service';

const CreateProductSchema = z.object({
  body: z.object({
    title: z.string().min(2, 'Title is required'),
    slug: z.string().min(2, 'Slug is required'),
    description: z.string().min(10, 'Description is required'),
    categoryId: z.string().uuid('Category ID is required'),
    occasionId: z.string().uuid().optional().nullable(),
    basePrice: z.number().min(0, 'Base price must be >= 0'),
    compareAtPrice: z.number().optional().nullable(),
    isVegetarian: z.boolean().default(true),
    isPersonalized: z.boolean().default(false),
    personalizationPrompt: z.string().optional().nullable(),
    isSameDayEligible: z.boolean().default(true),
    isMidnightEligible: z.boolean().default(true),
    variants: z
      .array(
        z.object({
          title: z.string().min(1),
          price: z.number().min(0),
          compareAtPrice: z.number().optional(),
          sku: z.string().min(1),
          stockQuantity: z.number().int().default(100),
          weightOrSize: z.string().optional(),
          isDefault: z.boolean().default(false),
        })
      )
      .min(1, 'At least one variant is required'),
    images: z
      .array(
        z.object({
          imageUrl: z.string().url(),
          isPrimary: z.boolean().default(false),
          sortOrder: z.number().int().default(0),
        })
      )
      .min(1, 'At least one image is required'),
    addOnIds: z.array(z.string().uuid()).optional(),
  }),
});

export const adminProductsRouter = Router();

adminProductsRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// GET /products
adminProductsRouter.get('/', async (req: Request, res: Response) => {
  try {
    const { categoryId, search, page, limit } = req.query;
    const pageNum = page ? Number(page) : 1;
    const limitNum = limit ? Number(limit) : 20;
    const skip = (pageNum - 1) * limitNum;

    const where: any = {};
    if (categoryId) where.categoryId = categoryId as string;
    if (search) {
      where.OR = [
        { title: { contains: String(search), mode: 'insensitive' } },
        { slug: { contains: String(search), mode: 'insensitive' } },
      ];
    }

    const [total, products] = await Promise.all([
      prisma.product.count({ where }),
      prisma.product.findMany({
        where,
        include: {
          category: true,
          occasion: true,
          variants: true,
          images: { orderBy: { sortOrder: 'asc' } },
          addOns: true,
        },
        orderBy: { createdAt: 'desc' },
        skip,
        take: limitNum,
      }),
    ]);

    return ApiResponse.paginated(res, products, total, pageNum, limitNum);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_PRODUCTS');
  }
});

// POST /products
adminProductsRouter.post('/', validate(CreateProductSchema), async (req: Request, res: Response) => {
  try {
    const {
      title,
      slug,
      description,
      categoryId,
      occasionId,
      basePrice,
      compareAtPrice,
      isVegetarian,
      isPersonalized,
      personalizationPrompt,
      isSameDayEligible,
      isMidnightEligible,
      variants,
      images,
      addOnIds,
    } = req.body;

    const product = await prisma.product.create({
      data: {
        title,
        slug,
        description,
        categoryId,
        occasionId: occasionId || null,
        basePrice,
        compareAtPrice: compareAtPrice || null,
        isVegetarian,
        isPersonalized,
        personalizationPrompt: personalizationPrompt || null,
        isSameDayEligible,
        isMidnightEligible,
        variants: {
          create: variants,
        },
        images: {
          create: images,
        },
        ...(addOnIds &&
          addOnIds.length > 0 && {
            addOns: {
              connect: addOnIds.map((id: string) => ({ id })),
            },
          }),
      },
      include: {
        variants: true,
        images: true,
        addOns: true,
      },
    });

    eventBus.broadcast('PRODUCT_UPDATED', { action: 'create', id: product.id });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'product', action: 'create', id: product.id });

    return ApiResponse.success(res, product, 201);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_CREATE_PRODUCT');
  }
});

// PUT /products/:id
adminProductsRouter.put('/:id', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const {
      title,
      description,
      categoryId,
      occasionId,
      basePrice,
      compareAtPrice,
      isVegetarian,
      isPersonalized,
      personalizationPrompt,
      isSameDayEligible,
      isMidnightEligible,
      isActive,
    } = req.body;

    const updated = await prisma.product.update({
      where: { id },
      data: {
        ...(title && { title }),
        ...(description && { description }),
        ...(categoryId && { categoryId }),
        ...(occasionId !== undefined && { occasionId }),
        ...(basePrice !== undefined && { basePrice }),
        ...(compareAtPrice !== undefined && { compareAtPrice }),
        ...(isVegetarian !== undefined && { isVegetarian }),
        ...(isPersonalized !== undefined && { isPersonalized }),
        ...(personalizationPrompt !== undefined && { personalizationPrompt }),
        ...(isSameDayEligible !== undefined && { isSameDayEligible }),
        ...(isMidnightEligible !== undefined && { isMidnightEligible }),
        ...(isActive !== undefined && { isActive }),
      },
      include: {
        variants: true,
        images: true,
        category: true,
      },
    });

    eventBus.broadcast('PRODUCT_UPDATED', { action: 'update', id: updated.id });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'product', action: 'update', id: updated.id });

    return ApiResponse.success(res, updated);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_UPDATE_PRODUCT');
  }
});

// DELETE /products/:id (toggle active or permanent delete)
adminProductsRouter.delete('/:id', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    const { permanent } = req.query;

    const product = await prisma.product.findUnique({ where: { id } });
    if (!product) {
      return ApiResponse.error(res, 'Product not found', 404, 'NOT_FOUND');
    }

    if (permanent === 'true') {
      // Check if product is referenced by orders
      const orderItemCount = await prisma.orderItem.count({ where: { productId: id } });
      if (orderItemCount > 0) {
        // Can't delete due to order history, deactivate instead
        const updated = await prisma.product.update({
          where: { id },
          data: { isActive: false },
        });
        eventBus.broadcast('PRODUCT_UPDATED', { action: 'deactivate', id });
        eventBus.broadcast('CATALOG_UPDATED', { entity: 'product', action: 'deactivate', id });
        return ApiResponse.success(res, {
          message: 'Product is referenced by past orders and was deactivated instead of deleted',
          isActive: updated.isActive,
        });
      }

      await prisma.product.delete({ where: { id } });
      eventBus.broadcast('PRODUCT_UPDATED', { action: 'delete', id });
      eventBus.broadcast('CATALOG_UPDATED', { entity: 'product', action: 'delete', id });
      return ApiResponse.success(res, { message: 'Product deleted permanently' });
    }

    const updated = await prisma.product.update({
      where: { id },
      data: { isActive: !product.isActive },
    });

    eventBus.broadcast('PRODUCT_UPDATED', { action: 'toggle', id, isActive: updated.isActive });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'product', action: 'toggle', id, isActive: updated.isActive });

    return ApiResponse.success(res, {
      message: `Product is now ${updated.isActive ? 'active' : 'inactive'}`,
      isActive: updated.isActive,
    });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_DELETE_PRODUCT');
  }
});
