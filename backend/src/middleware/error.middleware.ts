import { Request, Response, NextFunction } from 'express';
import { ApiResponse } from '../utils/api-response';
import { logger } from '../utils/logger';

export const errorHandler = (err: any, req: Request, res: Response, next: NextFunction) => {
  logger.error('Unhandled Exception:', {
    message: err.message,
    stack: err.stack,
    url: req.originalUrl,
    method: req.method,
  });

  const statusCode = err.statusCode || 500;
  const message = err.message || 'An unexpected internal server error occurred';
  const code = err.code || 'INTERNAL_SERVER_ERROR';

  return ApiResponse.error(
    res,
    message,
    statusCode,
    code,
    process.env.NODE_ENV === 'development' ? err.stack : undefined
  );
};
