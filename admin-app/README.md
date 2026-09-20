# Wishify Admin Android App

The dedicated operations, store management, and order fulfillment Android application for the Wishify platform.

## Branding & Visual Identity
- **Primary Color**: Dark Plum `#2D112C`
- **Accent Color**: Gold `#FFC107`
- **Dark Surface**: Slate `#1E1E24`
- Visually distinguished from the customer app, optimized for store staff, warehouse pickers, and fulfillment managers.

## Tech Stack & Security
- **Language**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose & Material 3
- **Authentication**: Email & Password login with Role-Based Access Control (`ADMIN` or `SUPER_ADMIN`).
- **Biometric Security**: AndroidX Biometric prompt (Fingerprint / Face ID unlock).
- **Networking**: Retrofit 2 + OkHttp 4 + Kotlinx Serialization.
- **Persistence**: DataStore Preferences for secure session token storage.
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## Key Features
- **Real-Time Operations Dashboard**:
  - Today's Revenue and Order Volume KPIs.
  - Pending deliveries count with urgent action indicators.
  - Low stock warning counter for variants requiring restocking.
  - Top 5 selling gifts list.
  - 30-day daily revenue trend chart.
- **Order Fulfillment Pipeline**:
  - Filterable tabs: `ALL`, `PLACED`, `CONFIRMED`, `PREPARED`, `OUT_FOR_DELIVERY`, `DELIVERED`, `CANCELLED`.
  - Step-by-step pipeline status transitions:
    - `PLACED` → Accept & Confirm
    - `CONFIRMED` → Mark as Prepared & Packed
    - `PREPARED` → Assign Delivery Partner & Dispatch
    - `OUT_FOR_DELIVERY` → Mark Delivered
  - Every status change automatically writes an audit log to `OrderStatusHistory` and dispatches a push notification to the customer's device.
  - Delivery partner assignment (Partner Name, Contact Phone, Tracking Number).
  - Refund processing for cancelled or returned orders.
- **Product & Inventory Management**:
  - Live inventory listing with search.
  - Instant Active/Inactive toggle.
  - Real-time variant stock counts with low-stock warnings.
- **Customer Directory**:
  - Customer list with order counts and lifetime spend.
- **Review Moderation**:
  - Customer review queue with Approve / Reject moderation.
- **Broadcast Notifications**:
  - Compose and broadcast promotional FCM push notifications directly to all active customer devices.
