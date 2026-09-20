import { Router, Request, Response } from 'express';
import multer from 'multer';
import { prisma } from '../../../services/prisma.service';
import { CloudinaryService } from '../../../services/cloudinary.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
});

export const adminSettingsRouter = Router();

adminSettingsRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// GET /api/v1/admin/settings/payment
adminSettingsRouter.get('/payment', async (_req: Request, res: Response) => {
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

// PUT /api/v1/admin/settings/payment
adminSettingsRouter.put('/payment', async (req: Request, res: Response) => {
  try {
    const { upiId, qrImageUrl, accountHolderName, codMaxAmount } = req.body;

    let settings = await prisma.paymentSetting.findFirst();
    if (!settings) {
      settings = await prisma.paymentSetting.create({
        data: {
          upiId: upiId || 'wishify@upi',
          qrImageUrl:
            qrImageUrl ||
            'https://api.qrserver.com/v1/create-qr-code/?size=300x300&data=upi://pay?pa=wishify@upi&pn=Wishify&cu=INR',
          accountHolderName: accountHolderName || 'Wishify Gifts',
          codMaxAmount: codMaxAmount !== undefined ? Number(codMaxAmount) : 100.0,
        },
      });
    } else {
      settings = await prisma.paymentSetting.update({
        where: { id: settings.id },
        data: {
          upiId: upiId !== undefined ? upiId : settings.upiId,
          qrImageUrl: qrImageUrl !== undefined ? qrImageUrl : settings.qrImageUrl,
          accountHolderName:
            accountHolderName !== undefined ? accountHolderName : settings.accountHolderName,
          codMaxAmount:
            codMaxAmount !== undefined ? Number(codMaxAmount) : settings.codMaxAmount,
        },
      });
    }

    return ApiResponse.success(res, settings);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'UPDATE_SETTINGS_FAILED');
  }
});

// POST /api/v1/admin/settings/payment/upload-qr
adminSettingsRouter.post(
  '/payment/upload-qr',
  upload.single('image'),
  async (req: Request, res: Response) => {
    try {
      if (!req.file) {
        return ApiResponse.error(res, 'No QR image file uploaded', 400, 'NO_FILE_UPLOADED');
      }

      const result = await CloudinaryService.uploadImage(req.file.buffer, 'wishify/payment_qr');

      let settings = await prisma.paymentSetting.findFirst();
      if (!settings) {
        settings = await prisma.paymentSetting.create({
          data: {
            upiId: 'wishify@upi',
            qrImageUrl: result.url,
            accountHolderName: 'Wishify Gifts',
            codMaxAmount: 100.0,
          },
        });
      } else {
        settings = await prisma.paymentSetting.update({
          where: { id: settings.id },
          data: { qrImageUrl: result.url },
        });
      }

      return ApiResponse.success(res, settings, 200);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'UPLOAD_QR_FAILED');
    }
  }
);
