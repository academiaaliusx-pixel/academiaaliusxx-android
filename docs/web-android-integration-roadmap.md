# Web + Android integration roadmap

## Confirmed infrastructure
- Cloudflare Pages project: `academiaaliusx-app`
- Production domain: `academiaaliusx.com`
- Pages Functions are enabled.
- Production Pages binding uses D1 database `academiaaliusx-db`.
- Android repository is available; the web source repository is not available in the current GitHub connection.

## Guardrails
- Do not guess API routes or auth/session semantics.
- Do not put privileged credentials in the Android app.
- Do not perform production writes or schema changes during integration discovery.
- Use staging for integration tests after its schema is reconciled with production.
- HTTPS only; keep local/offline behavior available.

## Implementation sequence
1. Obtain access to the web/Pages Functions source and verify endpoint/auth contract.
2. Reconcile staging schema with the features to be integrated.
3. Build a typed Android API layer and test against staging.
4. Integrate authentication and account/profile read operations.
5. Integrate tournaments and registration.
6. Integrate social features (friends, teams, messaging) and notifications.
7. Add synchronization, offline/error states, and security tests.
8. Verify production behavior only after staging tests and explicit authorization.

## Current blocker
Cloudflare metadata and D1 schema inspection do not reveal the deployed Pages Function route handlers or their request/response contracts. The Android client must not implement speculative live writes until these are verified.
