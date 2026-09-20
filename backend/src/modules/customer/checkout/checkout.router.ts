import { Router } from 'express';
import { CustomerCheckoutController } from './checkout.controller';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { validate } from '../../../middleware/validate.middleware';
import { InitiateCheckoutSchema, VerifyPaymentSchema } from './checkout.validation';

export const customerCheckoutRouter = Router();

customerCheckoutRouter.use(authenticateJwt);

customerCheckoutRouter.post('/initiate', validate(InitiateCheckoutSchema), CustomerCheckoutController.initiate);
customerCheckoutRouter.post('/verify', validate(VerifyPaymentSchema), CustomerCheckoutController.verify);
