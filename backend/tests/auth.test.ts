import request from 'supertest';
import { createApp } from '../src/app';
import { prisma } from '../src/services/prisma.service';
import * as bcrypt from 'bcryptjs';

jest.mock('../src/services/prisma.service', () => ({
  prisma: {
    user: {
      findUnique: jest.fn(),
      findFirst: jest.fn(),
      create: jest.fn(),
      update: jest.fn(),
    },
    cart: {
      create: jest.fn(),
    },
    notification: {
      create: jest.fn(),
    },
    deviceToken: {
      findMany: jest.fn().mockResolvedValue([]),
    },
  },
}));

const app = createApp();

describe('Authentication Flow', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Customer Phone OTP Auth', () => {
    it('POST /api/v1/auth/request-otp should return success and test OTP flag', async () => {
      const res = await request(app)
        .post('/api/v1/auth/request-otp')
        .send({ phone: '+919876543210' });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.message).toBeDefined();
    });

    it('POST /api/v1/auth/verify-otp with valid OTP should issue access and refresh tokens', async () => {
      const mockUser = {
        id: 'user-uuid-1234',
        phone: '+919876543210',
        email: null,
        name: null,
        role: 'CUSTOMER',
        walletBalance: 0,
        isActive: true,
      };

      (prisma.user.findUnique as jest.Mock).mockResolvedValue(mockUser);

      const res = await request(app)
        .post('/api/v1/auth/verify-otp')
        .send({ phone: '+919876543210', otp: '123456' });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.tokens.accessToken).toBeDefined();
      expect(res.body.data.tokens.refreshToken).toBeDefined();
      expect(res.body.data.user.id).toBe(mockUser.id);
    });

    it('POST /api/v1/auth/verify-otp with invalid OTP format should return validation error', async () => {
      const res = await request(app)
        .post('/api/v1/auth/verify-otp')
        .send({ phone: '+919876543210', otp: '12' });

      expect(res.status).toBe(422);
      expect(res.body.success).toBe(false);
      expect(res.body.error.code).toBe('VALIDATION_ERROR');
    });
  });

  describe('Admin Email/Password Auth', () => {
    it('POST /api/v1/admin/auth/login with valid credentials should return tokens', async () => {
      const passwordHash = await bcrypt.hash('AdminPassword123!', 10);
      const mockAdmin = {
        id: 'admin-uuid-1',
        email: 'admin@wishify.in',
        name: 'Super Admin',
        role: 'SUPER_ADMIN',
        passwordHash,
        isActive: true,
      };

      (prisma.user.findUnique as jest.Mock).mockResolvedValue(mockAdmin);

      const res = await request(app)
        .post('/api/v1/admin/auth/login')
        .send({ email: 'admin@wishify.in', password: 'AdminPassword123!' });

      expect(res.status).toBe(200);
      expect(res.body.success).toBe(true);
      expect(res.body.data.tokens.accessToken).toBeDefined();
      expect(res.body.data.user.role).toBe('SUPER_ADMIN');
    });

    it('POST /api/v1/admin/auth/login with invalid password should fail', async () => {
      const passwordHash = await bcrypt.hash('AdminPassword123!', 10);
      const mockAdmin = {
        id: 'admin-uuid-1',
        email: 'admin@wishify.in',
        name: 'Super Admin',
        role: 'SUPER_ADMIN',
        passwordHash,
        isActive: true,
      };

      (prisma.user.findUnique as jest.Mock).mockResolvedValue(mockAdmin);

      const res = await request(app)
        .post('/api/v1/admin/auth/login')
        .send({ email: 'admin@wishify.in', password: 'WrongPassword' });

      expect(res.status).toBe(401);
      expect(res.body.success).toBe(false);
    });
  });
});
