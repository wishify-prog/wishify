# Wishify: Architectural Decision Records (ADRs)

This document records the architectural design choices, trade-offs, and defaults selected across the Wishify platform.

---

## ADR-001: Monorepo Repository Structure
- **Context**: The project requires a shared backend and two native Android apps (Customer and Admin).
- **Decision**: Organize the codebase as a clean monorepo containing `backend/`, `customer-app/`, `admin-app/`, and `docs/`.
- **Consequences**: Ensures single source of truth for the API contract (`docs/architecture.md`), simplifies cross-app version alignment, and allows streamlined local development and CI/CD pipelines.

---

## ADR-002: Authentication & Token Lifecycle
- **Context**: Customer app utilizes phone OTP authentication; Admin app utilizes email/password and biometric unlock.
- **Decision**:
  - Implement JWT authentication with dual tokens: short-lived Access Token (15 minutes) and long-lived Refresh Token (30 days).
  - For phone OTP, the backend includes an OTP generation engine with a development fallback (`123456` or simulated SMS logs) when an SMS gateway (e.g. Twilio/MSG91) is not configured in `.env`.
  - Admin login uses bcrypt hashed passwords and enforces role checks (`ADMIN` or `SUPER_ADMIN`).
  - Android apps store tokens securely using encrypted DataStore and implement `TokenAuthenticator` in OkHttp to refresh tokens seamlessly upon receiving HTTP 401.

---

## ADR-003: Backend Database & Business Logic Sovereignty
- **Context**: Gift pricing, coupon discounts, slot cut-offs, and inventory reservation must not be duplicated or manipulated by mobile clients.
- **Decision**:
  - The backend PostgreSQL database and Express/Prisma services are the sole authority for business rules.
  - Cart totals, discounts, taxes, and delivery fees are recalculated on the server during checkout initiation.
  - Stock validation and deduction use Prisma transactions (`$transaction`) with optimistic/pessimistic safeguards.

---

## ADR-004: Payment Handling (Razorpay & COD)
- **Context**: Users can pay via Razorpay (UPI, Cards, Netbanking) or Cash on Delivery (COD).
- **Decision**:
  - Orders are initiated with `PENDING` payment status.
  - When Razorpay is selected, the backend creates a Razorpay Order ID via the SDK, and the customer app launches the native Razorpay checkout sheet.
  - The backend verifies the HMAC SHA256 signature (`razorpay_order_id`, `razorpay_payment_id`, `razorpay_signature`) before updating the order status to `CONFIRMED` and payment status to `PAID`.
  - In local development mode without valid Razorpay API keys, a test/mock verification mode is available to simulate successful checkout.

---

## ADR-005: Delivery Slot & Pincode Serviceability Engine
- **Context**: Gifting orders require scheduling with specific date and slot combinations (Standard, Fixed Time, Midnight, Early Morning) and serviceable delivery pincodes.
- **Decision**:
  - Pincodes define serviceability, same-day delivery eligibility, and delivery fees.
  - Delivery slots have cut-off hours (e.g., standard slot cut-off is 2 hours before start time; same-day orders after 6 PM cannot select early slots).
  - The backend dynamically calculates available slots for a given pincode and date.

---

## ADR-006: Android Theming & Design System
- **Context**: Both apps belong to the Wishify brand family but serve different user personas.
- **Decision**:
  - **Wishify Customer App**: Rose primary (`#E91E63`), blush background (`#FFF0F5`), plum typography (`#4A154B`), and gold accents (`#FFD700`). Warm, celebratory, and approachable.
  - **Wishify Admin App**: Dark plum (`#2D112C`), accent gold (`#FFC107`), and dark slate surface (`#1E1E24`). Professional, high-contrast, designed for rapid operational tasks in warehouse or store environments.

---

## ADR-007: Media Storage via Cloudinary with Local Fallback
- **Context**: Products, categories, and banners require image assets.
- **Decision**:
  - Provide a Cloudinary upload service. If Cloudinary credentials are missing in development, fallback to serving static assets or returning seeded placeholder URLs.
