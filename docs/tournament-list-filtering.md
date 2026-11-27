# Native tournament list filters

The native tournaments screen now filters the existing in-memory response by a text query across tournament title, game, status and date. Three local filters are available: all, open, and joined.

- No new endpoint or server mutation is introduced.
- Filtering is applied to the list already loaded by the existing API client; the backend contract is still unverified.
- Empty server list and no-match filter state are distinguished.
- Filter rule tests cover case-insensitive search, combined filters and common joined-value encodings.
- Registration/cancellation remains gated by the existing UI and unverified API route; this feature does not validate those write contracts.

Next: inspect the available verified tournament fields and add a detail screen without assuming an endpoint. If a detail endpoint cannot be evidenced, keep the detail scope limited to the list response and explicitly document missing fields.
