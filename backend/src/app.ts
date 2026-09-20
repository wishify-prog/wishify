import express, { Express, Request, Response } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import morgan from 'morgan';
import swaggerUi from 'swagger-ui-express';
import fs from 'fs';
import path from 'path';

import { ENV } from './config/env';
import { errorHandler } from './middleware/error.middleware';

// Customer Routers
import { customerAuthRouter } from './modules/customer/auth/auth.router';
import { customerAddressRouter } from './modules/customer/address/address.router';
import { customerCatalogRouter } from './modules/customer/catalog/catalog.router';
import { customerDeliveryRouter } from './modules/customer/delivery/delivery.router';
import { customerCartRouter } from './modules/customer/cart/cart.router';
import { customerCouponRouter } from './modules/customer/coupons/coupon.router';
import { customerCheckoutRouter } from './modules/customer/checkout/checkout.router';
import { customerOrderRouter } from './modules/customer/orders/order.router';
import { customerWishlistRouter } from './modules/customer/wishlist/wishlist.router';
import { customerReminderRouter } from './modules/customer/reminders/reminders.router';
import { customerNotificationRouter } from './modules/customer/notifications/notifications.router';

// Admin Routers
import { adminAuthRouter } from './modules/admin/auth/admin-auth.router';
import { adminDashboardRouter } from './modules/admin/dashboard/dashboard.router';
import { adminOrdersRouter } from './modules/admin/orders/admin-orders.router';
import { adminProductsRouter } from './modules/admin/products/admin-products.router';
import { adminMediaRouter } from './modules/admin/media/media.router';
import { adminCatalogRouter } from './modules/admin/catalog/admin-catalog.router';
import { adminCustomersRouter } from './modules/admin/customers/admin-customers.router';
import { adminReviewsRouter } from './modules/admin/reviews/admin-reviews.router';
import { adminBroadcastRouter } from './modules/admin/broadcast/admin-broadcast.router';
import { adminUsersRouter } from './modules/admin/users/admin-users.router';

import { eventBus } from './services/event-bus.service';

export const createApp = (): Express => {
  const app = express();

  // Core Middleware
  app.use(helmet({ contentSecurityPolicy: false }));
  app.use(cors({ origin: ENV.CORS_ORIGIN, credentials: true }));
  app.use(express.json({ limit: '10mb' }));
  app.use(express.urlencoded({ extended: true, limit: '10mb' }));

  if (ENV.NODE_ENV !== 'test') {
    app.use(morgan('combined'));
  }

  // Health check endpoint
  app.get('/health', (_req: Request, res: Response) => {
    res.status(200).json({
      status: 'UP',
      service: 'wishify-backend',
      timestamp: new Date().toISOString(),
      uptime: process.uptime(),
      sseClients: eventBus.getClientCount(),
    });
  });

  // Real-time Server-Sent Events (SSE) stream for Customer & Admin apps
  app.get('/api/v1/events', (req: Request, res: Response) => {
    res.setHeader('Content-Type', 'text/event-stream');
    res.setHeader('Cache-Control', 'no-cache');
    res.setHeader('Connection', 'keep-alive');
    res.setHeader('X-Accel-Buffering', 'no');
    res.flushHeaders();

    eventBus.registerClient(res);

    const keepAlive = setInterval(() => {
      res.write(': keep-alive\n\n');
    }, 20000);

    req.on('close', () => {
      clearInterval(keepAlive);
    });
  });

  // Swagger UI Documentation
  try {
    const swaggerDocument = JSON.parse(
      fs.readFileSync(path.join(__dirname, '../swagger.json'), 'utf8')
    );
    app.use('/docs', swaggerUi.serve, swaggerUi.setup(swaggerDocument));
  } catch (err) {
    // If swagger.json is not found in root, fallback
    console.warn('Swagger documentation not loaded:', err);
  }

  // Customer Routes (/api/v1)
  app.use('/api/v1/auth', customerAuthRouter);
  app.use('/api/v1/addresses', customerAddressRouter);
  app.use('/api/v1', customerCatalogRouter);
  app.use('/api/v1', customerDeliveryRouter);
  app.use('/api/v1/cart', customerCartRouter);
  app.use('/api/v1/coupons', customerCouponRouter);
  app.use('/api/v1/checkout', customerCheckoutRouter);
  app.use('/api/v1/orders', customerOrderRouter);
  app.use('/api/v1/wishlist', customerWishlistRouter);
  app.use('/api/v1/reminders', customerReminderRouter);
  app.use('/api/v1/notifications', customerNotificationRouter);

  // Admin Routes (/api/v1/admin)
  app.use('/api/v1/admin/auth', adminAuthRouter);
  app.use('/api/v1/admin/dashboard', adminDashboardRouter);
  app.use('/api/v1/admin/orders', adminOrdersRouter);
  app.use('/api/v1/admin/products', adminProductsRouter);
  app.use('/api/v1/admin/media', adminMediaRouter);
  app.use('/api/v1/admin', adminCatalogRouter);
  app.use('/api/v1/admin/customers', adminCustomersRouter);
  app.use('/api/v1/admin/reviews', adminReviewsRouter);
  app.use('/api/v1/admin/broadcast', adminBroadcastRouter);
  app.use('/api/v1/admin/users', adminUsersRouter);

  // Global Error Handler
  app.use(errorHandler);

  return app;
};
