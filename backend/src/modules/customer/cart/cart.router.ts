import { Router } from 'express';
import { CustomerCartController } from './cart.controller';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { validate } from '../../../middleware/validate.middleware';
import { AddCartItemSchema, UpdateCartItemSchema } from './cart.validation';

export const customerCartRouter = Router();

customerCartRouter.use(authenticateJwt);

customerCartRouter.get('/', CustomerCartController.getCart);
customerCartRouter.post('/items', validate(AddCartItemSchema), CustomerCartController.addItem);
customerCartRouter.put('/items/:id', validate(UpdateCartItemSchema), CustomerCartController.updateItem);
customerCartRouter.delete('/items/:id', CustomerCartController.removeItem);
