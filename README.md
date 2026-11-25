# Academia Alius X — Android nativo

Kotlin + Jetpack Compose. No WebView and no redirect to the website.

## Current native MVP

- Bottom navigation: Home, Community, Tournaments, Profile.
- Profile name and selected games persist on-device.
- Tournament sample cards support local sign-up/cancel actions, persisted on-device.
- Feature data is explicitly demo/local until verified API contracts are available.

## Backend integration safety

A read-only audit of Cloudflare Pages/D1 schema metadata has been documented in [docs/backend-read-only-audit.md](docs/backend-read-only-audit.md). The audit did not read user records or modify production. API routes/auth contracts and a reconciled staging environment remain blockers for live integration.

The native app is developed in small commits with GitHub Actions APK builds and APK signature/archive verification.
