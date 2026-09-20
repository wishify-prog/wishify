import { Request, Response } from 'express';
import { CustomerAuthService } from './auth.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerAuthController {
  static async requestOtp(req: Request, res: Response) {
    try {
      const { phone } = req.body;
      const result = await CustomerAuthService.requestOtp(phone);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'OTP_REQUEST_FAILED');
    }
  }

  static async verifyOtp(req: Request, res: Response) {
    try {
      const { phone, otp } = req.body;
      const result = await CustomerAuthService.verifyOtp(phone, otp);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      if (error.message === 'INVALID_OR_EXPIRED_OTP') {
        return ApiResponse.error(res, 'Invalid or expired OTP', 400, 'INVALID_OTP');
      }
      return ApiResponse.error(res, error.message, 400, 'OTP_VERIFICATION_FAILED');
    }
  }

  static async refreshToken(req: Request, res: Response) {
    try {
      const { refreshToken } = req.body;
      const result = await CustomerAuthService.refreshToken(refreshToken);
      return ApiResponse.success(res, result);
    } catch (error: any) {
      return ApiResponse.error(res, 'Invalid or expired refresh token', 401, 'INVALID_REFRESH_TOKEN');
    }
  }

  static async getProfile(req: Request, res: Response) {
    try {
      const user = await CustomerAuthService.getProfile(req.user!.id);
      return ApiResponse.success(res, user);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 404, 'USER_NOT_FOUND');
    }
  }

  static async updateProfile(req: Request, res: Response) {
    try {
      const updated = await CustomerAuthService.updateProfile(req.user!.id, req.body);
      return ApiResponse.success(res, updated);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'UPDATE_PROFILE_FAILED');
    }
  }
}
