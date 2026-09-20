import { Router, Request, Response } from 'express';
import { prisma } from '../../../services/prisma.service';
import { ApiResponse } from '../../../utils/api-response';
import { authenticateJwt } from '../../../middleware/auth.middleware';
import { requireRoles } from '../../../middleware/role.middleware';
import { Role } from '@prisma/client';
import { eventBus } from '../../../services/event-bus.service';

export const adminCatalogRouter = Router();

adminCatalogRouter.use(authenticateJwt, requireRoles(Role.ADMIN, Role.SUPER_ADMIN));

// ==========================================
// --- CATEGORIES ---
// ==========================================
adminCatalogRouter.get('/categories', async (_req: Request, res: Response) => {
  const items = await prisma.category.findMany({ orderBy: { sortOrder: 'asc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/categories', async (req: Request, res: Response) => {
  try {
    const item = await prisma.category.create({ data: req.body });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'category', action: 'create', id: item.id });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_CATEGORY');
  }
});

adminCatalogRouter.put('/categories/:id', async (req: Request, res: Response) => {
  try {
    const item = await prisma.category.update({ where: { id: req.params.id }, data: req.body });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'category', action: 'update', id: item.id });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_CATEGORY');
  }
});

adminCatalogRouter.delete('/categories/:id', async (req: Request, res: Response) => {
  try {
    await prisma.category.delete({ where: { id: req.params.id } });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'category', action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Category deleted successfully' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_CATEGORY');
  }
});

// ==========================================
// --- OCCASIONS ---
// ==========================================
adminCatalogRouter.get('/occasions', async (_req: Request, res: Response) => {
  const items = await prisma.occasion.findMany({ orderBy: { sortOrder: 'asc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/occasions', async (req: Request, res: Response) => {
  try {
    const item = await prisma.occasion.create({ data: req.body });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'occasion', action: 'create', id: item.id });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_OCCASION');
  }
});

adminCatalogRouter.put('/occasions/:id', async (req: Request, res: Response) => {
  try {
    const item = await prisma.occasion.update({ where: { id: req.params.id }, data: req.body });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'occasion', action: 'update', id: item.id });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_OCCASION');
  }
});

adminCatalogRouter.delete('/occasions/:id', async (req: Request, res: Response) => {
  try {
    await prisma.occasion.delete({ where: { id: req.params.id } });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'occasion', action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Occasion deleted successfully' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_OCCASION');
  }
});

// ==========================================
// --- BANNERS ---
// ==========================================
adminCatalogRouter.get('/banners', async (_req: Request, res: Response) => {
  const items = await prisma.banner.findMany({ orderBy: { sortOrder: 'asc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/banners', async (req: Request, res: Response) => {
  try {
    const item = await prisma.banner.create({ data: req.body });
    eventBus.broadcast('BANNER_UPDATED', { action: 'create', id: item.id });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_BANNER');
  }
});

adminCatalogRouter.put('/banners/:id', async (req: Request, res: Response) => {
  try {
    const item = await prisma.banner.update({ where: { id: req.params.id }, data: req.body });
    eventBus.broadcast('BANNER_UPDATED', { action: 'update', id: item.id });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_BANNER');
  }
});

adminCatalogRouter.delete('/banners/:id', async (req: Request, res: Response) => {
  try {
    await prisma.banner.delete({ where: { id: req.params.id } });
    eventBus.broadcast('BANNER_UPDATED', { action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Banner deleted' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_BANNER');
  }
});

// ==========================================
// --- COUPONS ---
// ==========================================
adminCatalogRouter.get('/coupons', async (_req: Request, res: Response) => {
  const items = await prisma.coupon.findMany({ orderBy: { createdAt: 'desc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/coupons', async (req: Request, res: Response) => {
  try {
    const item = await prisma.coupon.create({
      data: {
        ...req.body,
        validFrom: req.body.validFrom ? new Date(req.body.validFrom) : new Date(),
        validTo: new Date(req.body.validTo),
      },
    });
    eventBus.broadcast('COUPON_UPDATED', { action: 'create', id: item.id, code: item.code });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_COUPON');
  }
});

adminCatalogRouter.put('/coupons/:id', async (req: Request, res: Response) => {
  try {
    const data = { ...req.body };
    if (data.validFrom) data.validFrom = new Date(data.validFrom);
    if (data.validTo) data.validTo = new Date(data.validTo);
    const item = await prisma.coupon.update({ where: { id: req.params.id }, data });
    eventBus.broadcast('COUPON_UPDATED', { action: 'update', id: item.id, code: item.code });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_COUPON');
  }
});

adminCatalogRouter.delete('/coupons/:id', async (req: Request, res: Response) => {
  try {
    await prisma.coupon.delete({ where: { id: req.params.id } });
    eventBus.broadcast('COUPON_UPDATED', { action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Coupon deleted successfully' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_COUPON');
  }
});

// ==========================================
// --- PINCODES ---
// ==========================================
adminCatalogRouter.get('/pincodes', async (_req: Request, res: Response) => {
  const items = await prisma.pincode.findMany({ orderBy: { code: 'asc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/pincodes', async (req: Request, res: Response) => {
  try {
    const item = await prisma.pincode.upsert({
      where: { code: req.body.code },
      update: req.body,
      create: req.body,
    });
    eventBus.broadcast('PINCODE_UPDATED', { action: 'upsert', code: item.code });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_PINCODE');
  }
});

adminCatalogRouter.put('/pincodes/:id', async (req: Request, res: Response) => {
  try {
    const item = await prisma.pincode.update({ where: { id: req.params.id }, data: req.body });
    eventBus.broadcast('PINCODE_UPDATED', { action: 'update', code: item.code });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_PINCODE');
  }
});

adminCatalogRouter.delete('/pincodes/:id', async (req: Request, res: Response) => {
  try {
    await prisma.pincode.delete({ where: { id: req.params.id } });
    eventBus.broadcast('PINCODE_UPDATED', { action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Pincode deleted successfully' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_PINCODE');
  }
});

// ==========================================
// --- DELIVERY SLOTS ---
// ==========================================
adminCatalogRouter.get('/slots', async (_req: Request, res: Response) => {
  const items = await prisma.deliverySlot.findMany({ orderBy: { startTime: 'asc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/slots', async (req: Request, res: Response) => {
  try {
    const item = await prisma.deliverySlot.create({ data: req.body });
    eventBus.broadcast('SLOT_UPDATED', { action: 'create', id: item.id });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_SLOT');
  }
});

adminCatalogRouter.put('/slots/:id', async (req: Request, res: Response) => {
  try {
    const item = await prisma.deliverySlot.update({ where: { id: req.params.id }, data: req.body });
    eventBus.broadcast('SLOT_UPDATED', { action: 'update', id: item.id });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_SLOT');
  }
});

adminCatalogRouter.delete('/slots/:id', async (req: Request, res: Response) => {
  try {
    await prisma.deliverySlot.delete({ where: { id: req.params.id } });
    eventBus.broadcast('SLOT_UPDATED', { action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Slot deleted successfully' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_SLOT');
  }
});

// ==========================================
// --- ADD-ONS ---
// ==========================================
adminCatalogRouter.get('/addons', async (_req: Request, res: Response) => {
  const items = await prisma.addOn.findMany({ orderBy: { createdAt: 'desc' } });
  return ApiResponse.success(res, items);
});

adminCatalogRouter.post('/addons', async (req: Request, res: Response) => {
  try {
    const item = await prisma.addOn.create({ data: req.body });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'addon', action: 'create', id: item.id });
    return ApiResponse.success(res, item, 201);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_CREATE_ADDON');
  }
});

adminCatalogRouter.put('/addons/:id', async (req: Request, res: Response) => {
  try {
    const item = await prisma.addOn.update({ where: { id: req.params.id }, data: req.body });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'addon', action: 'update', id: item.id });
    return ApiResponse.success(res, item);
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_UPDATE_ADDON');
  }
});

adminCatalogRouter.delete('/addons/:id', async (req: Request, res: Response) => {
  try {
    await prisma.addOn.delete({ where: { id: req.params.id } });
    eventBus.broadcast('CATALOG_UPDATED', { entity: 'addon', action: 'delete', id: req.params.id });
    return ApiResponse.success(res, { message: 'Addon deleted successfully' });
  } catch (err: any) {
    return ApiResponse.error(res, err.message, 400, 'FAILED_TO_DELETE_ADDON');
  }
});
