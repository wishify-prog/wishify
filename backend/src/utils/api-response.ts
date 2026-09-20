import { Response } from 'express';

export interface ApiResponseFormat<T> {
  success: boolean;
  data: T | null;
  error: {
    code: string;
    message: string;
    details?: any;
  } | null;
  meta?: {
    page?: number;
    limit?: number;
    total?: number;
    totalPages?: number;
  };
}

export class ApiResponse {
  static success<T>(res: Response, data: T, statusCode: number = 200, meta?: any): Response {
    const body: ApiResponseFormat<T> = {
      success: true,
      data,
      error: null,
      ...(meta && { meta }),
    };
    return res.status(statusCode).json(body);
  }

  static paginated<T>(
    res: Response,
    items: T[],
    total: number,
    page: number,
    limit: number,
    statusCode: number = 200
  ): Response {
    const totalPages = Math.ceil(total / limit);
    return res.status(statusCode).json({
      success: true,
      data: items,
      error: null,
      meta: {
        page,
        limit,
        total,
        totalPages,
      },
    });
  }

  static error(
    res: Response,
    message: string,
    statusCode: number = 400,
    code: string = 'BAD_REQUEST',
    details?: any
  ): Response {
    const body: ApiResponseFormat<null> = {
      success: false,
      data: null,
      error: {
        code,
        message,
        details: details || null,
      },
    };
    return res.status(statusCode).json(body);
  }
}
