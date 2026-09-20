import { prisma } from '../../../services/prisma.service';
import { CONSTANTS } from '../../../config/constants';

export class CustomerCatalogService {
  static async getHomeFeed() {
    const [banners, categories, occasions, trending, bestSellers, sameDay, under999] = await Promise.all([
      prisma.banner.findMany({
        where: { isActive: true },
        orderBy: { sortOrder: 'asc' },
      }),
      prisma.category.findMany({
        where: { isActive: true },
        orderBy: { sortOrder: 'asc' },
      }),
      prisma.occasion.findMany({
        where: { isActive: true },
        orderBy: { sortOrder: 'asc' },
      }),
      // Trending (high review count)
      prisma.product.findMany({
        where: { isActive: true },
        include: { images: true, variants: true },
        orderBy: { reviewCount: 'desc' },
        take: 8,
      }),
      // Best sellers (high rating)
      prisma.product.findMany({
        where: { isActive: true },
        include: { images: true, variants: true },
        orderBy: { averageRating: 'desc' },
        take: 8,
      }),
      // Same-day eligible
      prisma.product.findMany({
        where: { isActive: true, isSameDayEligible: true },
        include: { images: true, variants: true },
        take: 8,
      }),
      // Under ₹999
      prisma.product.findMany({
        where: { isActive: true, basePrice: { lte: 999 } },
        include: { images: true, variants: true },
        orderBy: { basePrice: 'asc' },
        take: 8,
      }),
    ]);

    return {
      banners,
      categories,
      occasions,
      sections: [
        { id: 'trending', title: 'Trending Gifts', subtitle: 'Most loved by our community', products: trending },
        { id: 'best-sellers', title: 'Best Sellers', subtitle: 'Top rated for life’s moments', products: bestSellers },
        { id: 'same-day', title: 'Same-Day Delivery', subtitle: 'Delivered today with care', products: sameDay },
        { id: 'under-999', title: 'Under ₹999', subtitle: 'Pocket-friendly surprises', products: under999 },
      ],
    };
  }

  static async listCategories() {
    return prisma.category.findMany({
      where: { isActive: true },
      orderBy: { sortOrder: 'asc' },
    });
  }

  static async listOccasions() {
    return prisma.occasion.findMany({
      where: { isActive: true },
      orderBy: { sortOrder: 'asc' },
    });
  }

  static async listProducts(filter: {
    categoryId?: string;
    occasionId?: string;
    minPrice?: number;
    maxPrice?: number;
    isVegetarian?: boolean;
    isSameDay?: boolean;
    isMidnight?: boolean;
    rating?: number;
    sort?: string;
    page?: number;
    limit?: number;
  }) {
    const page = filter.page && filter.page > 0 ? Number(filter.page) : 1;
    const limit = filter.limit && filter.limit > 0 ? Math.min(Number(filter.limit), CONSTANTS.MAX_PAGE_SIZE) : CONSTANTS.DEFAULT_PAGE_SIZE;
    const skip = (page - 1) * limit;

    const where: any = { isActive: true };

    if (filter.categoryId) where.categoryId = filter.categoryId;
    if (filter.occasionId) where.occasionId = filter.occasionId;
    if (filter.minPrice !== undefined || filter.maxPrice !== undefined) {
      where.basePrice = {};
      if (filter.minPrice !== undefined) where.basePrice.gte = Number(filter.minPrice);
      if (filter.maxPrice !== undefined) where.basePrice.lte = Number(filter.maxPrice);
    }
    if (filter.isVegetarian !== undefined) where.isVegetarian = filter.isVegetarian === true || filter.isVegetarian === ('true' as any);
    if (filter.isSameDay !== undefined) where.isSameDayEligible = filter.isSameDay === true || filter.isSameDay === ('true' as any);
    if (filter.isMidnight !== undefined) where.isMidnightEligible = filter.isMidnight === true || filter.isMidnight === ('true' as any);
    if (filter.rating) where.averageRating = { gte: Number(filter.rating) };

    let orderBy: any = { createdAt: 'desc' };
    if (filter.sort === 'price_asc') orderBy = { basePrice: 'asc' };
    else if (filter.sort === 'price_desc') orderBy = { basePrice: 'desc' };
    else if (filter.sort === 'rating_desc') orderBy = { averageRating: 'desc' };
    else if (filter.sort === 'popularity') orderBy = { reviewCount: 'desc' };

    const [total, products] = await Promise.all([
      prisma.product.count({ where }),
      prisma.product.findMany({
        where,
        include: {
          images: { orderBy: { sortOrder: 'asc' } },
          variants: true,
          category: { select: { id: true, name: true, slug: true } },
          occasion: { select: { id: true, name: true, slug: true } },
        },
        orderBy,
        skip,
        take: limit,
      }),
    ]);

    return { total, page, limit, products };
  }

  static async searchProducts(query: string) {
    if (!query || query.trim().length === 0) {
      return { suggestions: [], products: [] };
    }

    const trimmed = query.trim();
    const products = await prisma.product.findMany({
      where: {
        isActive: true,
        OR: [
          { title: { contains: trimmed, mode: 'insensitive' } },
          { description: { contains: trimmed, mode: 'insensitive' } },
          { category: { name: { contains: trimmed, mode: 'insensitive' } } },
        ],
      },
      include: {
        images: true,
        variants: true,
      },
      take: 15,
    });

    const suggestions = products.map((p) => p.title).slice(0, 5);

    return { suggestions, products };
  }

  static async getProductBySlug(slug: string) {
    const product = await prisma.product.findUnique({
      where: { slug },
      include: {
        images: { orderBy: { sortOrder: 'asc' } },
        variants: { orderBy: { price: 'asc' } },
        addOns: { where: { isActive: true } },
        category: true,
        occasion: true,
        reviews: {
          where: { status: 'APPROVED' },
          include: { user: { select: { id: true, name: true } } },
          orderBy: { createdAt: 'desc' },
          take: 10,
        },
      },
    });

    if (!product || !product.isActive) {
      throw new Error('PRODUCT_NOT_FOUND');
    }

    // Similar products in same category
    const similarProducts = await prisma.product.findMany({
      where: {
        categoryId: product.categoryId,
        id: { not: product.id },
        isActive: true,
      },
      include: { images: true, variants: true },
      take: 6,
    });

    return { product, similarProducts };
  }

  static async submitReview(userId: string, productId: string, data: { rating: number; comment?: string }) {
    const product = await prisma.product.findUnique({ where: { id: productId } });
    if (!product) {
      throw new Error('PRODUCT_NOT_FOUND');
    }

    return prisma.review.create({
      data: {
        productId,
        userId,
        rating: data.rating,
        comment: data.comment,
        status: 'PENDING', // Queued for admin moderation
      },
    });
  }
}
