import { Router } from 'express';
import { CustomerCouponController } from './coupon.controller';

export const customerCouponRouter = Router();

customerCouponRouter.get('/', CustomerCouponController.list);
customerCouponRouter.post('/validate', CustomerCouponController.validate);
