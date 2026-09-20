# Wishify Backend API

High-performance REST API service for the Wishify Gifting Marketplace Platform built with Node.js, TypeScript, Express, and Prisma ORM.

## Tech Stack
- **Runtime**: Node.js 20+ & TypeScript 5+
- **Database**: PostgreSQL with Prisma ORM
- **Authentication**: JWT (Dual Token: Access 15m + Refresh 30d) with Role-Based Access Control (`CUSTOMER`, `ADMIN`, `SUPER_ADMIN`)
- **Validation**: Zod schema validation
- **Documentation**: Swagger UI / OpenAPI 3.0 at `/docs`
- **Payment Gateway**: Razorpay (Order generation, HMAC-SHA256 signature verification) & Cash on Delivery (COD)
- **Media Storage**: Cloudinary CDN with fallback simulator
- **Push Notifications**: Firebase Cloud Messaging (FCM) & in-app notification database
- **Deployment**: Railway container (`railway.json`, `Dockerfile`, `/health`)

## Available Scripts
```bash
npm run dev           # Start development server with hot-reload
npm run build         # Compile TypeScript to dist/
npm start             # Start compiled server in production
npm run prisma:seed   # Seed 32+ realistic products, categories, coupons, and users
npm test              # Run automated integration tests with Jest
```

## Endpoints Overview
- **Customer API (`/api/v1`)**: Auth OTP, profile, addresses, curated home feed, search, product filters, pincode check, delivery slots, cart math, coupons, checkout, orders timeline, cancel, wishlist, reminders, reviews.
- **Admin API (`/api/v1/admin`)**: Operations dashboard KPIs, 30-day sales chart, live order pipeline status transitions, delivery partner assignment, refunds, product inventory, catalog CRUD, customer metrics, review moderation, broadcast push.
