# Native Android parity checklist

Legend: **[x]** verified complete, **[~]** partially implemented / not yet verified end-to-end, **[ ]** missing.

Last reviewed: 27 November 2026, branch `integration/aax-web-api`.

## Account and identity
- [~] Login, registration and password-recovery screens/calls exist; deployed endpoint and auth contracts are unverified.
- [~] Email verification, resend and token-reset methods exist in the API client; dedicated UI/deep-link handling is not complete.
- [~] Session cookie lifecycle now includes encrypted Android Keystore persistence, startup restore and local clearing on HTTP 401/sign-out; server-side expiry semantics and device behavior remain unverified.

## Profile and game catalog
- [~] Profile read, statistics and avatar-frame selection are present; endpoint contracts are unverified.
- [ ] Profile editing, avatar upload/removal UI and comprehensive account preferences are not complete.
- [~] Static game catalog and tournament grouping/filtering are present; catalog source and synchronization are not authoritative.

## Competition
- [~] Tournament list and registration/cancellation UI/API calls exist; route contracts are unverified. Native list filtering by title/game/status and open/registered state is implemented locally and covered by JVM tests. A local detail dialog now displays fields present in list responses; a dedicated server detail/results route remains unverified/missing.
- [~] Search, local filters and local detail presentation are implemented; server detail, result reporting, creation/management and consistent error/loading/empty states still need work.
- [~] Ranking and recent-podium views call API methods; contracts and presentation completeness remain unverified.

## Community
- [~] Player search, friend request/accept/reject/remove, blocking, teams and team-request actions have UI/API calls; contracts remain unverified.
- [ ] Direct messaging UI, message state and notifications are missing.

## Organization
- [~] Challonge connection-test UI exists; other roster, match, state, link and synchronization methods are API-client-only and not end-to-end verified.
- [ ] Admin authorization UX, safe preview/review, and full management workflows need verification and completion.

## Settings, offline, accessibility
- [~] Change-password, block-list/unblock, account-deletion and logout UI/API calls exist; live contracts are unverified. Destructive production actions are not to be tested.
- [~] Local profile/tournament/backup rule modules and tests exist; end-to-end offline behavior and integration with the screens need audit.
- [ ] Deep links and push/local notification flows.
- [~] Basic text labels and Compose controls are present; accessibility audit and UI tests are not complete.

## Release and integration blockers
- [~] CI builds and APK verification workflow exists. A previous run failed because JVM tests were invoking Android's stub `org.json`; this run adds a real JSON test dependency and expands tests. The final commit's CI result is pending.
- [ ] Obtain deployed Pages Function source or a verified endpoint/auth contract.
- [ ] Reconcile staging D1 schema with production before any write integration testing.
- [ ] Verify API calls against staging, then conduct a production-read-only smoke check only after contract verification and authorization.
- [ ] Complete screen-level UI tests, release build, APK verification, and device testing.

## Immediate next cycle
1. Verify CI for local tournament detail dialog.
2. Continue tournament result/management gaps only when endpoint contracts are evidenced.
3. Audit session restore/401 and navigation UI states with tests; device-level Keystore behavior remains unverified.
4. Continue contract discovery without production writes; do not label speculative calls as verified.
