import { Request, Response } from 'express';
import { CustomerCatalogService } from './catalog.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerCatalogController {
  static async getHomeFeed(req: Request, res: Response) {
    try {
      const feed = await CustomerCatalogService.getHomeFeed();
      return ApiResponse.success(res, feed);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_HOME_FEED');
    }
  }

  static async listCategories(req: Request, res: Response) {
    try {
      const categories = await CustomerCatalogService.listCategories();
      return ApiResponse.success(res, categories);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LIST_CATEGORIES');
    }
  }

  static async listOccasions(req: Request, res: Response) {
    try {
      const occasions = await CustomerCatalogService.listOccasions();
      return ApiResponse.success(res, occasions);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LIST_OCCASIONS');
    }
  }

  static async listProducts(req: Request, res: Response) {
    try {
      const { categoryId, occasionId, minPrice, maxPrice, isVegetarian, isSameDay, isMidnight, rating, sort, page, limit } = req.query;
      const result = await CustomerCatalogService.listProducts({
        categoryId: categoryId as string,
        occasionId: occasionId as string,
        minPrice: minPrice ? Number(minPrice) : undefined,
        maxPrice: maxPrice ? Number(maxPrice) : undefined,
        isVegetarian: isVegetarian ? isVegetarian === 'true' : undefined,
        isSameDay: isSameDay ? isSameDay === 'true' : undefined,
        isMidnight: isMidnight ? isMidnight === 'true' : undefined,
        rating: rating ? Number(rating) : undefined,
        sort: sort as string,
        page: page ? Number(page) : undefined,
        limit: limit ? Number(limit) : undefined,
      });

      return ApiResponse.paginated(res, result.products, result.total, result.page, result.limit);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LIST_PRODUCTS');
    }
  }

  static async search(req: Request, res: Response) {
    try {
      const query = (req.query.q as string) || '';
      const result = await CustomerCatalogService.searchProducts(query);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'SEARCH_FAILED');
    }
  }

  static async getProductDetail(req: Request, res: Response) {
    try {
      const result = await CustomerCatalogService.getProductBySlug(req.params.slug);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 404, 'PRODUCT_NOT_FOUND');
    }
  }

  static async submitReview(req: Request, res: Response) {
    try {
      const { rating, comment } = req.body;
      const review = await CustomerCatalogService.submitReview(req.user!.id, req.params.id, {
        rating: Number(rating),
        comment,
      });
      return ApiResponse.success(res, review, 201);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_SUBMIT_REVIEW');
    }
  }
}
