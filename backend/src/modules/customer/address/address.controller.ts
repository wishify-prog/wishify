import { Request, Response } from 'express';
import { CustomerAddressService } from './address.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerAddressController {
  static async list(req: Request, res: Response) {
    try {
      const addresses = await CustomerAddressService.listAddresses(req.user!.id);
      return ApiResponse.success(res, addresses);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_LIST_ADDRESSES');
    }
  }

  static async create(req: Request, res: Response) {
    try {
      const address = await CustomerAddressService.createAddress(req.user!.id, req.body);
      return ApiResponse.success(res, address, 201);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_CREATE_ADDRESS');
    }
  }

  static async update(req: Request, res: Response) {
    try {
      const address = await CustomerAddressService.updateAddress(req.user!.id, req.params.id, req.body);
      return ApiResponse.success(res, address);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_UPDATE_ADDRESS');
    }
  }

  static async delete(req: Request, res: Response) {
    try {
      await CustomerAddressService.deleteAddress(req.user!.id, req.params.id);
      return ApiResponse.success(res, { message: 'Address deleted successfully' });
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'FAILED_TO_DELETE_ADDRESS');
    }
  }
}
