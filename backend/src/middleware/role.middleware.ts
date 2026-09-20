import { Request, Response, NextFunction } from 'express';
import { Role } from '@prisma/client';
import { ApiResponse } from '../utils/api-response';

export const requireRoles = (...allowedRoles: Role[]) => {
  return (req: Request, res: Response, next: NextFunction): any => {
    if (!req.user) {
      return ApiResponse.error(res, 'Authentication required', 401, 'UNAUTHORIZED');
    }

    if (!allowedRoles.includes(req.user.role)) {
      return ApiResponse.error(
        res,
        `Access denied. Requires one of roles: [${allowedRoles.join(', ')}]`,
        403,
        'FORBIDDEN'
      );
    }

    return next();
  };
};
