import { Request, Response } from 'express';
import { CustomerCouponService } from './coupon.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerCouponController {
  static async list(req: Request, res: Response) {
    try {
      const coupons = await CustomerCouponService.listCoupons();
      return ApiResponse.success(res, coupons);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LIST_COUPONS');
    }
  }

  static async validate(req: Request, res: Response) {
    try {
      const { code, cartSubtotal } = req.body;
      if (!code) {
        return ApiResponse.error(res, 'Coupon code is required', 400, 'MISSING_CODE');
      }
      const result = await CustomerCouponService.validateCoupon(code, Number(cartSubtotal) || 0);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'INVALID_COUPON');
    }
  }
}
