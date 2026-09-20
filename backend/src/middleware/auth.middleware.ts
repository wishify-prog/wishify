import { Request, Response, NextFunction } from 'express';
import jwt from 'jsonwebtoken';
import { ENV } from '../config/env';
import { ApiResponse } from '../utils/api-response';
import { Role } from '@prisma/client';

export interface AuthenticatedUser {
  id: string;
  role: Role;
  phone?: string | null;
  email?: string | null;
}

declare global {
  namespace Express {
    interface Request {
      user?: AuthenticatedUser;
    }
  }
}

export const authenticateJwt = (req: Request, res: Response, next: NextFunction): any => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return ApiResponse.error(res, 'Authentication token missing or invalid format', 401, 'UNAUTHORIZED');
  }

  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, ENV.JWT_ACCESS_SECRET) as AuthenticatedUser;
    req.user = decoded;
    return next();
  } catch (error: any) {
    if (error.name === 'TokenExpiredError') {
      return ApiResponse.error(res, 'Access token has expired', 401, 'TOKEN_EXPIRED');
    }
    return ApiResponse.error(res, 'Invalid authentication token', 401, 'INVALID_TOKEN');
  }
};
