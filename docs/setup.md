# Wishify: Setup & Deployment Guide

This guide details the steps to set up, configure, run, and deploy the entire Wishify Gifting Platform.

---

## 1. Prerequisites
- **Node.js**: v20.x or higher
- **npm**: v10.x or higher
- **PostgreSQL**: v14.x or higher (local or managed like Railway / Supabase / RDS)
- **Java Development Kit (JDK)**: JDK 17 or higher
- **Android Studio / Android SDK**: Platform SDK 35, Build-Tools 35.0.0

---

## 2. Backend Local Setup

### 2.1 Install Dependencies & Generate Database Client
```bash
cd backend
npm install
npx prisma generate
```

### 2.2 Configure Environment Variables
Copy `.env.example` to `.env`:
```bash
cp .env.example .env
```
Edit `.env` with your PostgreSQL database URL and credentials:
```ini
PORT=4000
NODE_ENV=development
DATABASE_URL="postgresql://postgres:password@localhost:5432/wishify?schema=public"

JWT_ACCESS_SECRET="wishify_super_secret_access_key_2026_jwt"
JWT_REFRESH_SECRET="wishify_super_secret_refresh_key_2026_jwt"

# Third-Party Integrations (Optional in Dev - Mock Mode Active if left blank)
RAZORPAY_KEY_ID="rzp_test_..."
RAZORPAY_KEY_SECRET="..."

CLOUDINARY_CLOUD_NAME="..."
CLOUDINARY_API_KEY="..."
CLOUDINARY_API_SECRET="..."

FIREBASE_PROJECT_ID="..."
FIREBASE_CLIENT_EMAIL="..."
FIREBASE_PRIVATE_KEY="..."
```

### 2.3 Run Migrations & Seed Database
```bash
# Push schema to database
npx prisma db push

# Seed 32+ realistic products, categories, occasions, delivery slots, coupons, and users
npm run prisma:seed
```

### 2.4 Run Automated Tests
```bash
npm test
```

### 2.5 Start Development Server
```bash
npm run dev
```
- API Docs: `http://localhost:4000/docs`
- Health Endpoint: `http://localhost:4000/health`

---

## 3. Railway Deployment

Wishify Backend is pre-configured for one-click deployment on [Railway](https://railway.app) using `railway.json` and `Dockerfile`.

### 3.1 Via Railway Dashboard
1. Create a new Project on Railway.
2. Add a **PostgreSQL** database service to the project.
3. Add a new service from your GitHub repository pointing to the `/backend` directory.
4. Add the following Environment Variables in the Railway Service settings:
   - `DATABASE_URL`: `${{Postgres.DATABASE_URL}}`
   - `NODE_ENV`: `production`
   - `PORT`: `4000`
   - `JWT_ACCESS_SECRET`: `<generate a secure random 64-char string>`
   - `JWT_REFRESH_SECRET`: `<generate a secure random 64-char string>`
   - `RAZORPAY_KEY_ID`: `<your live or test Razorpay key>`
   - `RAZORPAY_KEY_SECRET`: `<your Razorpay secret>`
   - `CLOUDINARY_CLOUD_NAME`: `<your Cloudinary cloud name>`
   - `CLOUDINARY_API_KEY`: `<your Cloudinary key>`
   - `CLOUDINARY_API_SECRET`: `<your Cloudinary secret>`
   - `FIREBASE_PROJECT_ID`: `<your Firebase project ID>`
   - `FIREBASE_CLIENT_EMAIL`: `<service-account-email>`
   - `FIREBASE_PRIVATE_KEY`: `"<PEM formatted private key>"`
5. Deploy! Railway will automatically build the multi-stage Docker container and execute `npx prisma migrate deploy && node dist/server.js`.

---

## 4. Customer Android App Setup

### 4.1 Configuration
The customer app reads its backend API endpoint from `BuildConfig.BASE_URL`:
- For Android Emulator connecting to local backend: `http://10.0.2.2:4000/`
- For Physical Android Device connecting to local machine: `http://<your-local-ip>:4000/`
- For Production: `https://<your-railway-domain>.up.railway.app/`

You can override `BASE_URL` in `customer-app/local.properties`:
```properties
BASE_URL=https://wishify-api.railway.app/
```

### 4.2 Building & Running
```bash
cd customer-app
./gradlew assembleDebug
```
To install directly to a connected device:
```bash
./gradlew installDebug
```

---

## 5. Admin Android App Setup

### 5.1 Configuration
The admin app connects to the same backend instance. Configure `admin-app/local.properties`:
```properties
BASE_URL=https://wishify-api.railway.app/
```

### 5.2 Building & Running
```bash
cd admin-app
./gradlew assembleDebug
```

### 5.3 Default Admin Credentials (from Seeding)
- **Email**: `admin@wishify.in`
- **Password**: `AdminPassword123!`
- **Role**: `SUPER_ADMIN`
- **Biometric Unlock**: Supported on devices with biometric sensors.
