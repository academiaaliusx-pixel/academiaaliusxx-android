# Read-only backend audit (25 November 2026)

## Scope and safety

This audit inspected Cloudflare Pages project/deployment metadata and D1 schema metadata only. It did **not** read user rows, change schema, write data, alter Pages/Workers deployments, or modify the live website.

## Production D1 schema observed

The production schema includes these feature areas:

- `users`, `sessions`, `auth_tokens`, `auth_attempts`: account/session infrastructure.
- `friendships`, `social_blocks`: social graph and blocking.
- `teams`, `team_members`, `team_requests`: teams.
- `tournaments`, `entries`, `results`, `staax_matches`, `staax_match_audit`, `staax_admin_audit`: tournament registration and bracket/results.
- `direct_messages`: messaging.
- `player_aliases`, `nickname_change_log`, `season_avatar_awards`: profile-related features.

No row data was read. Schema queries reported zero rows written.

## Staging schema drift

The staging database schema is not a complete mirror of production as observed in this audit. The staging schema does not currently include several production feature tables, including `direct_messages`, `friendships`, `social_blocks`, `teams`, `team_members`, and `team_requests`. It also has a tournament/match schema that differs from production.

## Integration blocker

The Pages project uses Functions and binds D1, but the read-only Cloudflare metadata audit did not expose the route handlers or the app's API route/auth contract. The native app must not guess API routes, authentication semantics, session/token handling, or write payloads.

Before implementing live requests, inspect the Pages Functions source or obtain a verified endpoint inventory and request/response contract. Prefer staging, but first reconcile staging schema drift. Any integration test that could create, modify, or delete user data must remain out of scope until explicitly authorized.

## Native client direction

- Keep native UI and local persistence available offline.
- Use HTTPS only; do not permit cleartext traffic.
- Do not ship production credentials or privileged secrets in the APK.
- Treat session storage, token expiry/refresh, error handling, and account deletion as security-sensitive.
- Never perform schema changes or production writes from the mobile client.


## Follow-up read-only audit (26 November 2026)

- The Pages project metadata still reports `uses_functions: true`, and the latest production deployment metadata remains the deployment dated 24 November 2026 at 19:02 UTC.
- The account-level Workers script listing returned an empty list.
- Direct read-only retrieval of `pages-worker--23556599-production` and `pages-worker--23556599-preview` returned HTTP 404 with Cloudflare error `10007` (`This Worker does not exist on your account`). These Pages-managed scripts are not exposed as independently retrievable Workers through the available API.
- No deployment, project setting, D1 schema, or row data was changed during this follow-up.

The API route and auth contract remains unverified. The native app must continue to avoid live account, tournament, messaging, and profile writes until handlers or a verified contract become available.
