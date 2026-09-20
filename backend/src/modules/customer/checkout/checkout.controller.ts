import { Request, Response } from 'express';
import { CustomerCheckoutService } from './checkout.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerCheckoutController {
  static async initiate(req: Request, res: Response) {
    try {
      const result = await CustomerCheckoutService.initiateCheckout(req.user!.id, req.body);
      return ApiResponse.success(res, result, 201);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'CHECKOUT_INITIATE_FAILED');
    }
  }

  static async verify(req: Request, res: Response) {
    try {
      const result = await CustomerCheckoutService.verifyPayment(req.user!.id, req.body);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'PAYMENT_VERIFICATION_FAILED');
    }
  }
}
