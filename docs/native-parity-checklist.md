# Native Android parity checklist

Legend: **[x]** complete and verified; **[~]** implemented in part/local only/not verified online; **[ ]** missing.

Last reviewed: 27 November 2026, branch `integration/aax-web-api`.

## Native app shell
- [~] Compose navigation: Inicio, Torneos, Comunidad, Perfil; current UI cycle is native rather than WebView. Screen-level tests and device validation pending.
- [~] Offline-first local screen data and explicit local/demo labels; online parity remains unverified.

## Account and identity
- [~] Local profile display name and favorite games edit/persistence. Not a server profile.
- [ ] Online login, registration, email verification and recovery UI/flow verified end-to-end.
- [~] Session-cookie vault/API client exists in source, but route contracts and device behavior remain unverified; no live mutation testing.

## Profile and games
- [~] Editable local name, bounded input and supported-game selection.
- [ ] Online profile/statistics, profile editing, avatar upload/removal/frame and authoritative game catalogue.

## Competition
- [~] Native list with local/demo events, search, ALL/AVAILABLE/REGISTERED/CREATED filters.
- [~] Dedicated native detail screen with back navigation, local registration/withdrawal, sharing and local edit/delete actions (confirmed); created/sample data and actions remain device-local and do not publish or register online.
- [~] JSON backup/import validates local profile, favorite games, local registration IDs and created events; restoration now previews counts and requires explicit confirmation before replacing local data. The preview helper is unit-tested.
- [~] Deleting a locally created tournament now requires an explicit confirmation dialog.
- [ ] Server tournament list/detail, real join/leave, results, tournament administration, eligibility and verified loading/error/retry states against staging contracts.
- [~] Tournament filter and persistence/title rules have JVM coverage; Compose screen/device tests pending.
- [ ] Verified ranking/standings/podium integration.

## Community
- [ ] Verified native player search, friends/request actions, block/unblock, teams/requests and direct messages.
- [~] Community screen explains current contract blocker and links to official site without presenting mock player data.

## Organization
- [ ] Verified admin authorization and safe management flows; Challonge operations remain unverified and must not run against production.

## Settings, security, offline, accessibility
- [~] Local JSON export/import, bounded validation and user-visible import errors.
- [~] Local-only states labelled; no privileged secret shipped or production write performed in this cycle.
- [ ] Deep links, notifications, online settings/change-password/account deletion and device validation.
- [~] Compose controls have visible labels/content descriptions for primary navigation and share actions; instrumented UI tests added for navigation, detail/registration/back, local create-delete confirmation and profile editing. CI/device result pending.
- [ ] Full offline/restore behavior, device/emulator tests, release build and broader security review.

## Backend/CI blockers
- [ ] Obtain Pages Function handlers or verified API contract inventory; currently unavailable via read-only audit.
- [ ] Reconcile staging D1 schema with production before integration testing.
- [~] CI build workflow exists; this native-screen commit's run must be checked and any failures corrected.
- [ ] UI tests, install/run testing on device or emulator, signed release artifact, and end-to-end verification.

## Next cycle
1. Confirm CI emulator tests and fix any flaky semantics/device issues.
2. Audit backup restore and offline/persistence edge cases, and add test coverage for destructive confirmation paths.
3. Audit Android permissions, deep links, system navigation and accessibility.
4. Obtain read-only Pages Functions contract evidence; implement online features only once verified.
