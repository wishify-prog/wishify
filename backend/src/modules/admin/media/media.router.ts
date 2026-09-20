import { Router, Request, Response } from 'express';
import multer from 'multer';
import { CloudinaryService } from '../../../services/cloudinary.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';

const upload = multer({
  storage: multer.memoryStorage(),
  limits: { fileSize: 10 * 1024 * 1024 }, // 10MB
});

export const adminMediaRouter = Router();

adminMediaRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// POST /media/upload
adminMediaRouter.post('/upload', upload.single('image'), async (req: Request, res: Response) => {
  try {
    if (!req.file) {
      return ApiResponse.error(res, 'No image file uploaded', 400, 'NO_FILE_UPLOADED');
    }

    const folder = (req.body.folder as string) || 'wishify/products';
    const result = await CloudinaryService.uploadImage(req.file.buffer, folder);

    return ApiResponse.success(res, result, 201);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'UPLOAD_FAILED');
  }
});
