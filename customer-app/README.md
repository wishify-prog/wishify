# Wishify Customer Android App

The official Android customer application for the Wishify Gifting Marketplace Platform.

## Branding & Visual Identity
- **Tagline**: *"Wish it. Gift it. Delivered."*
- **Primary Color**: Rose `#E91E63`
- **Background**: Blush `#FFF0F5`
- **Typography**: Plum `#4A154B`
- **Accents**: Gold `#FFD700`
- Full Material 3 Light and Dark themes, rounded cards, and smooth transitions.
- Multi-language support: English and Hindi (`values-hi/strings.xml`).

## Architecture & Tech Stack
- **Language**: Kotlin 2.3+
- **UI Toolkit**: Jetpack Compose & Material 3
- **Architecture**: MVVM + Clean Architecture (Domain, Data, Presentation)
- **Networking**: Retrofit 2 + OkHttp 4 + Kotlinx Serialization
- **Image Loading**: Coil Compose
- **Persistence**: DataStore Preferences for token and pincode caching
- **Navigation**: Navigation Compose
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 35 (Android 15)

## Key Features
- **Phone OTP Authentication**: Easy mobile login with development fallback (`123456`) and guest browsing.
- **Dynamic Home Feed**: Pincode selector, search trigger, category icons, occasion chips, banner carousel, and curated sections (Trending Gifts, Best Sellers, Same-Day Delivery, Under ₹999).
- **Product Details**: Image gallery, variant picker (weight/size), add-ons checkboxes (cards, candles, teddy, chocolates), custom personalization text field, pincode delivery checker, date and delivery slot picker, reviews, and similar items.
- **Cart & Checkout**: Real-time pricing calculations, coupon application, saved delivery address picker, recipient details, gift message, anonymous sender toggle, and Razorpay / COD payment options.
- **Order Tracking**: Visual status timeline (Placed → Confirmed → Prepared → Out for Delivery → Delivered) with live updates, cancellation, and reordering.
- **Account**: Profile, Wishlist, Birthday & Anniversary reminders with custom lead-time alerts, and language toggle.
