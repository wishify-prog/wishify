import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';

export const customerPaymentSettingsRouter = Router();

// GET /api/v1/payment/settings
customerPaymentSettingsRouter.get('/settings', async (_req: Request, res: Response) => {
  try {
    let settings = await prisma.paymentSetting.findFirst();
    if (!settings) {
      settings = await prisma.paymentSetting.create({
        data: {
          upiId: 'wishify@upi',
          qrImageUrl:
            'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=upi://pay?pa=wishify@upi&pn=Wishify&cu=INR',
          accountHolderName: 'Wishify Gifts',
          codMaxAmount: 100.0,
        },
      });
    }

    return ApiResponse.success(res, settings);
  } catch (error: any) {
    // Graceful fallback if database table not yet migrated
    return ApiResponse.success(res, {
      id: 'default',
      upiId: 'wishify@upi',
      qrImageUrl:
        'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=upi://pay?pa=wishify@upi&pn=Wishify&cu=INR',
      accountHolderName: 'Wishify Gifts',
      codMaxAmount: 100.0,
    });
  }
});
