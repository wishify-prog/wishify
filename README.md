# Wishify: Gifting Marketplace Platform

> *"Wish it. Gift it. Delivered."*

Wishify is an end-to-end gifting marketplace platform inspired by Ferns N Petals (FNP). The platform enables customers to discover, customize, and schedule fresh flowers, cakes, plants, chocolates, personalized items, and celebration combos, delivered to recipients on specific dates and time slots.

The platform consists of three core applications sharing a single unified PostgreSQL database:
1. **`backend/`**: Node.js, TypeScript, Express, Prisma ORM, Zod validation, JWT authentication, Swagger UI, Razorpay & COD, Cloudinary, and Firebase Cloud Messaging, configured for Railway deployment.
2. **`customer-app/`**: Native Android customer application built with Kotlin, Jetpack Compose, Material 3 (Rose `#E91E63`, Blush, Plum, Gold), Clean Architecture, Retrofit, Coil, Room, DataStore, and Razorpay SDK.
3. **`admin-app/`**: Native Android operations & fulfillment application built with Kotlin, Jetpack Compose, Material 3 (Dark Plum `#2D112C` & Accent Gold `#FFC107`), Biometric authentication, live order status pipeline, catalog CRUD, customer metrics, and push broadcast.

---

## Monorepo Structure

```
wishify/
├── docs/
│   ├── architecture.md          # Complete system architecture, ER diagram, and API contract
│   ├── decisions.md             # Architectural Decision Records (ADRs)
│   └── setup.md                 # Detailed local setup and Railway deployment guide
├── backend/
│   ├── prisma/
│   │   ├── schema.prisma        # 22 relational models & enums
│   │   └── seed.ts              # Seed data: 32 realistic products, categories, coupons, admins
│   ├── src/
│   │   ├── config/              # Env & application constants
│   │   ├── middleware/          # Auth, role check, error handler, Zod validator
│   │   ├── modules/
│   │   │   ├── customer/        # Auth, Address, Catalog, Delivery, Cart, Coupons, Checkout, Orders, Wishlist, Reminders
│   │   │   └── admin/           # Auth, Dashboard, Orders, Products, Media, Catalog, Customers, Reviews, Broadcast, Users
│   │   ├── services/            # Prisma, Razorpay, Cloudinary, FCM services (with sandbox fallbacks)
│   │   ├── utils/               # Response envelope, Winston logger, order generator
│   │   ├── app.ts               # Express configuration & route mounting
│   │   └── server.ts            # HTTP entry point
│   ├── tests/                   # Jest / Supertest integration test suite
│   ├── swagger.json             # OpenAPI 3.0 specification
│   ├── railway.json             # Railway container deployment configuration
│   └── Dockerfile               # Multi-stage production container build
├── customer-app/                # Kotlin Jetpack Compose Customer App
│   ├── app/src/main/
│   │   ├── java/com/wishify/customer/
│   │   │   ├── data/            # Models, Retrofit API, NetworkClient, UserPreferences
│   │   │   ├── presentation/    # Home, Listing, Detail, Cart, Checkout, Orders, Auth, Account
│   │   │   └── ui/theme/        # Rose primary #E91E63, Blush, Plum, Gold Material 3 theme
│   │   └── res/
│   │       ├── values/          # English strings
│   │       └── values-hi/       # Hindi (हिन्दी) string resources
│   └── build.gradle.kts
├── admin-app/                   # Kotlin Jetpack Compose Admin App
│   ├── app/src/main/
│   │   ├── java/com/wishify/admin/
│   │   │   ├── data/            # Models, Retrofit API, AdminNetworkClient, AdminPreferences
│   │   │   ├── presentation/    # Dashboard, Orders Pipeline, Inventory, Customers, Reviews, Broadcast
│   │   │   └── ui/theme/        # Dark Plum #2D112C & Gold #FFC107 Material 3 theme
│   └── build.gradle.kts
└── README.md
```

---

## Quick Start Guide

### 1. Backend Service
```bash
cd backend
npm install
npx prisma generate
npm test
npm run dev
```
- API Documentation: `http://localhost:4000/docs`
- Health Check: `http://localhost:4000/health`

### 2. Wishify Customer Android App
Open `customer-app/` in Android Studio or compile via Gradle:
```bash
cd customer-app
./gradlew assembleDebug
```

### 3. Wishify Admin Android App
Open `admin-app/` in Android Studio or compile via Gradle:
```bash
cd admin-app
./gradlew assembleDebug
```

---

## Documentation Links
- [System Architecture & ER Diagram](docs/architecture.md)
- [Architectural Decision Records (ADRs)](docs/decisions.md)
- [Setup & Deployment Guide](docs/setup.md)
