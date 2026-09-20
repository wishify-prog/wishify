import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { ENV } from '../../../config/env';
import * as bcrypt from 'bcryptjs';
import jwt from 'jsonwebtoken';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';

const AdminLoginSchema = z.object({
  body: z.object({
    email: z.string().email('Valid email is required'),
    password: z.string().min(6, 'Password must be at least 6 characters'),
  }),
});

export const adminAuthRouter = Router();

// POST /api/v1/admin/auth/login
adminAuthRouter.post('/login', validate(AdminLoginSchema), async (req: Request, res: Response) => {
  try {
    const { email, password } = req.body;

    const user = await prisma.user.findUnique({
      where: { email },
    });

    if (!user || (user.role !== 'ADMIN' && user.role !== 'SUPER_ADMIN')) {
      return ApiResponse.error(res, 'Invalid credentials or unauthorized access', 401, 'INVALID_CREDENTIALS');
    }

    if (!user.passwordHash) {
      return ApiResponse.error(res, 'Password not set for this account', 401, 'PASSWORD_NOT_SET');
    }

    const isMatch = await bcrypt.compare(password, user.passwordHash);
    if (!isMatch) {
      return ApiResponse.error(res, 'Invalid credentials', 401, 'INVALID_CREDENTIALS');
    }

    const accessToken = jwt.sign(
      { id: user.id, role: user.role, email: user.email, name: user.name },
      ENV.JWT_ACCESS_SECRET,
      { expiresIn: '8h' }
    );

    const refreshToken = jwt.sign(
      { id: user.id, role: user.role },
      ENV.JWT_REFRESH_SECRET,
      { expiresIn: '30d' }
    );

    return ApiResponse.success(res, {
      user: {
        id: user.id,
        name: user.name,
        email: user.email,
        role: user.role,
      },
      tokens: {
        accessToken,
        refreshToken,
        expiresIn: 28800,
      },
    });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'ADMIN_LOGIN_FAILED');
  }
});
