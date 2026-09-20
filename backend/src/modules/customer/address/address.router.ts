import { Router } from 'express';
import { CustomerAddressController } from './address.controller';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { validate } from '../../../middleware/validate.middleware';
import { CreateAddressSchema, UpdateAddressSchema } from './address.validation';

export const customerAddressRouter = Router();

customerAddressRouter.use(authenticateJwt);

customerAddressRouter.get('/', CustomerAddressController.list);
customerAddressRouter.post('/', validate(CreateAddressSchema), CustomerAddressController.create);
customerAddressRouter.put('/:id', validate(UpdateAddressSchema), CustomerAddressController.update);
customerAddressRouter.delete('/:id', CustomerAddressController.delete);
