import { Router } from 'express';
import { CustomerDeliveryController } from './delivery.controller';

export const customerDeliveryRouter = Router();

customerDeliveryRouter.get('/pincode/check/:code', CustomerDeliveryController.checkPincode);
customerDeliveryRouter.get('/delivery-slots', CustomerDeliveryController.getSlots);
