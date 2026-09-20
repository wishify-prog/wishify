import { v2 as cloudinary } from 'cloudinary';
import { ENV } from '../config/env';
import { logger } from '../utils/logger';

export class CloudinaryService {
  private static isConfigured = false;

  private static init() {
    if (!this.isConfigured) {
      if (
        ENV.CLOUDINARY_CLOUD_NAME &&
        ENV.CLOUDINARY_API_KEY &&
        ENV.CLOUDINARY_API_SECRET &&
        !ENV.CLOUDINARY_CLOUD_NAME.includes('sample') &&
        !ENV.CLOUDINARY_API_KEY.includes('123456789')
      ) {
        cloudinary.config({
          cloud_name: ENV.CLOUDINARY_CLOUD_NAME,
          api_key: ENV.CLOUDINARY_API_KEY,
          api_secret: ENV.CLOUDINARY_API_SECRET,
        });
        this.isConfigured = true;
      }
    }
  }

  static async uploadImage(
    fileBuffer: Buffer,
    folder: string = 'wishify/products'
  ): Promise<{ url: string; publicId: string }> {
    this.init();

    if (!this.isConfigured) {
      const mockId = `mock_${Date.now()}_${Math.random().toString(36).substring(2, 6)}`;
      const mockUrl = `https://images.unsplash.com/photo-1513151233558-d860c5398176?w=800&mockId=${mockId}`;
      logger.info(`[MOCK CLOUDINARY] Stored image buffer, returning fallback URL: ${mockUrl}`);
      return {
        url: mockUrl,
        publicId: mockId,
      };
    }

    return new Promise((resolve, reject) => {
      const uploadStream = cloudinary.uploader.upload_stream(
        { folder, resource_type: 'image' },
        (error, result) => {
          if (error || !result) {
            logger.error('Cloudinary upload failed:', error);
            return reject(error);
          }
          resolve({
            url: result.secure_url,
            publicId: result.public_id,
          });
        }
      );
      uploadStream.end(fileBuffer);
    });
  }
}
