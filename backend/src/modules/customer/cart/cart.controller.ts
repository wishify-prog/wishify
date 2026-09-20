import { Request, Response } from 'express';
import { CustomerCartService } from './cart.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerCartController {
  static async getCart(req: Request, res: Response) {
    try {
      const cart = await CustomerCartService.getOrCreateCart(req.user!.id);
      return ApiResponse.success(res, cart);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_GET_CART');
    }
  }

  static async addItem(req: Request, res: Response) {
    try {
      const cart = await CustomerCartService.addItem(req.user!.id, req.body);
      return ApiResponse.success(res, cart, 201);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_ADD_ITEM');
    }
  }

  static async updateItem(req: Request, res: Response) {
    try {
      const cart = await CustomerCartService.updateItem(req.user!.id, req.params.id, req.body);
      return ApiResponse.success(res, cart);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_UPDATE_ITEM');
    }
  }

  static async removeItem(req: Request, res: Response) {
    try {
      const cart = await CustomerCartService.removeItem(req.user!.id, req.params.id);
      return ApiResponse.success(res, cart);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_REMOVE_ITEM');
    }
  }
}
