# Academia Alius X — Android nativo

Kotlin + Jetpack Compose. No WebView and no redirect to the website.

## Current native MVP

- Bottom navigation: Home, Community, Tournaments, Profile.
- Profile name and selected games persist on-device.
- Local tournament creation, sign-up/cancel and deletion; registrations and events persist on-device.\n- Tournament search and filters for all events, registered events and locally created events.
- Tournament title editing preserves spaces while typing and validates before saving.
- Unit tests cover profile rules and tournament title, add/remove and persistence rules.
- Feature data is explicitly demo/local until verified API contracts are available.

## Backend integration safety

A read-only audit of Cloudflare Pages/D1 schema metadata has been documented in [docs/backend-read-only-audit.md](docs/backend-read-only-audit.md). The audit did not read user records or modify production. API routes/auth contracts and a reconciled staging environment remain blockers for live integration.

The native app is developed in small commits with GitHub Actions APK builds and APK signature/archive verification.

**Live API calls are experimental.** The native client contains route calls that have not been verified against the deployed Pages Function handlers. Do not use account deletion, password changes, tournament/social mutations, or other write actions against production until the request/response and authorization contracts have been audited. CI unit tests do not establish backend compatibility.
