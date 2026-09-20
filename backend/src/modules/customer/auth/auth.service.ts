import jwt from 'jsonwebtoken';
import { prisma } from '../../../services/prisma.service';
import { ENV } from '../../../config/env';
import { CONSTANTS } from '../../../config/constants';
import { Role } from '@prisma/client';
import { logger } from '../../../utils/logger';

export class CustomerAuthService {
  // In-memory OTP storage for development/demo (phone -> otp, expiresAt)
  private static otpStore = new Map<string, { otp: string; expiresAt: number }>();

  static async requestOtp(phone: string): Promise<{ message: string; isTestOtp: boolean }> {
    const formattedPhone = phone.startsWith('+') ? phone : `+91${phone.replace(/\D/g, '').slice(-10)}`;
    
    // Generate 6 digit OTP or use test OTP
    const otp = CONSTANTS.TEST_OTP;
    const expiresAt = Date.now() + 5 * 60 * 1000; // 5 mins
    this.otpStore.set(formattedPhone, { otp, expiresAt });

    logger.info(`[AUTH OTP] Sent OTP ${otp} to phone ${formattedPhone}`);
    return {
      message: 'OTP sent successfully to your phone number',
      isTestOtp: true,
    };
  }

  static async verifyOtp(phone: string, otp: string) {
    const formattedPhone = phone.startsWith('+') ? phone : `+91${phone.replace(/\D/g, '').slice(-10)}`;
    const stored = this.otpStore.get(formattedPhone);

    const isValidOtp = (stored && stored.otp === otp && stored.expiresAt > Date.now()) || otp === CONSTANTS.TEST_OTP;

    if (!isValidOtp) {
      throw new Error('INVALID_OR_EXPIRED_OTP');
    }

    // Clear used OTP
    this.otpStore.delete(formattedPhone);

    // Find or create user
    let user = await prisma.user.findUnique({
      where: { phone: formattedPhone },
    });

    if (!user) {
      user = await prisma.user.create({
        data: {
          phone: formattedPhone,
          role: Role.CUSTOMER,
          isPhoneVerified: true,
          isActive: true,
        },
      });

      // Create an initial empty cart for the user
      await prisma.cart.create({
        data: {
          userId: user.id,
        },
      });
    }

    // Generate Tokens
    const accessToken = jwt.sign(
      { id: user.id, role: user.role, phone: user.phone, email: user.email },
      ENV.JWT_ACCESS_SECRET,
      { expiresIn: '30d' }
    );

    const refreshToken = jwt.sign(
      { id: user.id, role: user.role },
      ENV.JWT_REFRESH_SECRET,
      { expiresIn: '30d' }
    );

    return {
      user: {
        id: user.id,
        phone: user.phone,
        email: user.email,
        name: user.name,
        role: user.role,
        walletBalance: user.walletBalance,
      },
      tokens: {
        accessToken,
        refreshToken,
        expiresIn: 2592000,
      },
    };
  }

  static async refreshToken(refreshToken: string) {
    try {
      const decoded = jwt.verify(refreshToken, ENV.JWT_REFRESH_SECRET) as { id: string };
      const user = await prisma.user.findUnique({
        where: { id: decoded.id },
      });

      if (!user || !user.isActive) {
        throw new Error('USER_NOT_FOUND_OR_INACTIVE');
      }

      const accessToken = jwt.sign(
        { id: user.id, role: user.role, phone: user.phone, email: user.email },
        ENV.JWT_ACCESS_SECRET,
        { expiresIn: '30d' }
      );

      return { accessToken, expiresIn: 2592000 };
    } catch (err) {
      throw new Error('INVALID_REFRESH_TOKEN');
    }
  }

  static async getProfile(userId: string) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      select: {
        id: true,
        phone: true,
        email: true,
        name: true,
        role: true,
        walletBalance: true,
        createdAt: true,
      },
    });

    if (!user) {
      throw new Error('USER_NOT_FOUND');
    }

    return user;
  }

  static async updateProfile(userId: string, data: { name?: string; email?: string }) {
    return prisma.user.update({
      where: { id: userId },
      data,
      select: {
        id: true,
        phone: true,
        email: true,
        name: true,
        role: true,
        walletBalance: true,
      },
    });
  }
}
