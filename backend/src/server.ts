import { createApp } from './app';
import { ENV } from './config/env';
import { logger } from './utils/logger';

const app = createApp();

const server = app.listen(ENV.PORT, () => {
  logger.info(`🚀 Wishify Backend is running on port ${ENV.PORT} [${ENV.NODE_ENV}]`);
  logger.info(`📖 API Documentation available at http://localhost:${ENV.PORT}/docs`);
  logger.info(`❤️ Health check endpoint at http://localhost:${ENV.PORT}/health`);
});

// Graceful shutdown
process.on('SIGTERM', () => {
  logger.info('SIGTERM signal received: closing HTTP server');
  server.close(() => {
    logger.info('HTTP server closed');
  });
});

process.on('SIGINT', () => {
  logger.info('SIGINT signal received: closing HTTP server');
  server.close(() => {
    logger.info('HTTP server closed');
  });
});
