import { Router } from 'express';
import { CustomerAuthController } from './auth.controller';
import { validate } from '../../../middleware/validate.middleware';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import {
  RequestOtpSchema,
  VerifyOtpSchema,
  RefreshTokenSchema,
  UpdateProfileSchema,
} from './auth.validation';

export const customerAuthRouter = Router();

customerAuthRouter.post('/request-otp', validate(RequestOtpSchema), CustomerAuthController.requestOtp);
customerAuthRouter.post('/verify-otp', validate(VerifyOtpSchema), CustomerAuthController.verifyOtp);
customerAuthRouter.post('/refresh-token', validate(RefreshTokenSchema), CustomerAuthController.refreshToken);

customerAuthRouter.get('/me', authenticateJwt, CustomerAuthController.getProfile);
customerAuthRouter.put('/me', authenticateJwt, validate(UpdateProfileSchema), CustomerAuthController.updateProfile);
