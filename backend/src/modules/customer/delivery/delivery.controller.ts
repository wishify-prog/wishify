import { Request, Response } from 'express';
import { CustomerDeliveryService } from './delivery.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerDeliveryController {
  static async checkPincode(req: Request, res: Response) {
    try {
      const { code } = req.params;
      const result = await CustomerDeliveryService.checkPincode(code);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'PINCODE_CHECK_FAILED');
    }
  }

  static async getSlots(req: Request, res: Response) {
    try {
      const { pincode, date } = req.query;
      const slots = await CustomerDeliveryService.getAvailableSlots(
        pincode as string | undefined,
        date as string | undefined
      );
      return ApiResponse.success(res, slots);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'SLOT_FETCH_FAILED');
    }
  }
}
