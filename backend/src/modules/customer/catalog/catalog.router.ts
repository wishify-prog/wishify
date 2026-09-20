import { Router } from 'express';
import { CustomerCatalogController } from './catalog.controller';
import { authenticateJwt } from '../../../middleware/auth.middleware';

export const customerCatalogRouter = Router();

customerCatalogRouter.get('/home', CustomerCatalogController.getHomeFeed);
customerCatalogRouter.get('/categories', CustomerCatalogController.listCategories);
customerCatalogRouter.get('/occasions', CustomerCatalogController.listOccasions);
customerCatalogRouter.get('/products', CustomerCatalogController.listProducts);
customerCatalogRouter.get('/products/search', CustomerCatalogController.search);
customerCatalogRouter.get('/products/:slug', CustomerCatalogController.getProductDetail);
customerCatalogRouter.post('/products/:id/reviews', authenticateJwt, CustomerCatalogController.submitReview);
