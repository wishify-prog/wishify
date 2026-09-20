import { Router } from 'express';
import { CustomerOrderController } from './order.controller';
import { authenticateJwt } from '../../../middleware/auth.middleware';

export const customerOrderRouter = Router();

customerOrderRouter.use(authenticateJwt);

customerOrderRouter.get('/', CustomerOrderController.list);
customerOrderRouter.get('/:id', CustomerOrderController.getDetail);
customerOrderRouter.post('/:id/cancel', CustomerOrderController.cancel);
customerOrderRouter.get('/:id/invoice', CustomerOrderController.getInvoice);
