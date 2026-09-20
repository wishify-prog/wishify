import { Request, Response } from 'express';
import { CustomerOrderService } from './order.service';
import { ApiResponse } from '../../../utils/api-response';

export class CustomerOrderController {
  static async list(req: Request, res: Response) {
    try {
      const page = req.query.page ? Number(req.query.page) : 1;
      const limit = req.query.limit ? Number(req.query.limit) : 20;
      const result = await CustomerOrderService.listOrders(req.user!.id, page, limit);
      return ApiResponse.paginated(res, result.orders, result.total, result.page, result.limit);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LIST_ORDERS');
    }
  }

  static async getDetail(req: Request, res: Response) {
    try {
      const order = await CustomerOrderService.getOrderDetail(req.user!.id, req.params.id);
      return ApiResponse.success(res, order);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 404, 'ORDER_NOT_FOUND');
    }
  }

  static async cancel(req: Request, res: Response) {
    try {
      const { reason } = req.body;
      const order = await CustomerOrderService.cancelOrder(req.user!.id, req.params.id, reason);
      return ApiResponse.success(res, order);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 400, 'ORDER_CANCEL_FAILED');
    }
  }

  static async getInvoice(req: Request, res: Response) {
    try {
      const invoice = await CustomerOrderService.getInvoice(req.user!.id, req.params.id);
      return ApiResponse.success(res, invoice);
    } catch (error: any) {
      return ApiResponse.error(res, error.message, 404, 'INVOICE_NOT_FOUND');
    }
  }
}
