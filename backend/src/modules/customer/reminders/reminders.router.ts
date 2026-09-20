import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { z } from 'zod';
import { validate } from '../../../middleware/validate.middleware';

const CreateReminderSchema = z.object({
  body: z.object({
    occasionTitle: z.string().min(2, 'Occasion title is required'),
    recipientName: z.string().min(2, 'Recipient name is required'),
    eventDate: z.string().min(10, 'Event date is required (YYYY-MM-DD)'),
    remindDaysBefore: z.number().int().min(0).max(30).default(2),
  }),
});

export const customerReminderRouter = Router();
customerReminderRouter.use(authenticateJwt);

// GET /reminders
customerReminderRouter.get('/', async (req: Request, res: Response) => {
  try {
    const reminders = await prisma.reminder.findMany({
      where: { userId: req.user!.id },
      orderBy: { eventDate: 'asc' },
    });
    return ApiResponse.success(res, reminders);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 500, 'FAILED_TO_LOAD_REMINDERS');
  }
});

// POST /reminders
customerReminderRouter.post('/', validate(CreateReminderSchema), async (req: Request, res: Response) => {
  try {
    const { occasionTitle, recipientName, eventDate, remindDaysBefore } = req.body;
    const reminder = await prisma.reminder.create({
      data: {
        userId: req.user!.id,
        occasionTitle,
        recipientName,
        eventDate: new Date(eventDate),
        remindDaysBefore,
      },
    });
    return ApiResponse.success(res, reminder, 201);
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_CREATE_REMINDER');
  }
});

// DELETE /reminders/:id
customerReminderRouter.delete('/:id', async (req: Request, res: Response) => {
  try {
    const { id } = req.params;
    await prisma.reminder.deleteMany({
      where: { id, userId: req.user!.id },
    });
    return ApiResponse.success(res, { message: 'Reminder deleted successfully' });
  } catch (error: any) {
    return ApiResponse.error(res, error.message, 400, 'FAILED_TO_DELETE_REMINDER');
  }
});
