import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';
import { FcmService } from '../../../services/fcm.service';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';

const BroadcastSchema = z.object({
  body: z.object({
    title: z.string().min(2, 'Title is required'),
    body: z.string().min(5, 'Body is required'),
    type: z.string().default('PROMOTIONAL'),
    metadata: z.record(z.any()).optional(),
  }),
});

export const adminBroadcastRouter = Router();

adminBroadcastRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// POST /broadcast/push
adminBroadcastRouter.post('/push', validate(BroadcastSchema), async (req: Request, res: Response) => {
  try {
    const { title, body, type, metadata } = req.body;

    const customers = await prisma.user.findMany({
      where: { role: Role.CUSTOMER },
      select: { id: true },
    });

    // Send push & record notifications for all customers
    for (const customer of customers) {
      await FcmService.sendNotification(customer.id, title, body, type, metadata);
    }

    return ApiResponse.success(res, {
      message: `Broadcast initiated to ${customers.length} customers`,
      recipientCount: customers.length,
    });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'BROADCAST_FAILED');
  }
});
