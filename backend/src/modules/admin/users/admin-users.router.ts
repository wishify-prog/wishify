import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';
import * as bcrypt from 'bcryptjs';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';

const CreateAdminUserSchema = z.object({
  body: z.object({
    name: z.string().min(2, 'Name is required'),
    email: z.string().email('Valid email is required'),
    phone: z.string().optional(),
    password: z.string().min(6, 'Password must be at least 6 characters'),
    role: z.enum(['ADMIN', 'SUPER_ADMIN']).default('ADMIN'),
  }),
});

export const adminUsersRouter = Router();

adminUsersRouter.use(authenticateJwt, requireRoles(Role.SUPER_ADMIN));

// GET /users (List admins)
adminUsersRouter.get('/', async (_req: Request, res: Response) => {
  try {
    const admins = await prisma.user.findMany({
      where: { role: { in: [Role.ADMIN, Role.SUPER_ADMIN] } },
      select: {
        id: true,
        name: true,
        email: true,
        phone: true,
        role: true,
        isActive: true,
        createdAt: true,
      },
      orderBy: { createdAt: 'desc' },
    });
    return ApiResponse.success(res, admins);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_ADMINS');
  }
});

// POST /users (Create admin)
adminUsersRouter.post('/', validate(CreateAdminUserSchema), async (req: Request, res: Response) => {
  try {
    const { name, email, phone, password, role } = req.body;

    const existing = await prisma.user.findFirst({
      where: { OR: [{ email }, ...(phone ? [{ phone }] : [])] },
    });

    if (existing) {
      return ApiResponse.error(res, 'User with this email or phone already exists', 400, 'USER_EXISTS');
    }

    const salt = await bcrypt.genSalt(10);
    const passwordHash = await bcrypt.hash(password, salt);

    const newAdmin = await prisma.user.create({
      data: {
        name,
        email,
        phone: phone || null,
        passwordHash,
        role: role as Role,
        isPhoneVerified: true,
        isActive: true,
      },
      select: {
        id: true,
        name: true,
        email: true,
        phone: true,
        role: true,
        createdAt: true,
      },
    });

    return ApiResponse.success(res, newAdmin, 201);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_CREATE_ADMIN');
  }
});
