# Native Android session storage

## Behavior
- The native API client restores its cookie snapshot on app launch.
- Cookie JSON is bounded and validated for the website host before storage.
- Cookie snapshots are encrypted with AES-GCM using a non-exportable Android Keystore AES key; the SharedPreferences entry contains ciphertext only.
- Explicit sign-out attempts server logout, then clears the device-local cookie vault even if the server response is unavailable.
- HTTP 401 invalidates the local session vault.
- No cookie values or credentials are logged.

## Limits and validation
- Unit tests cover cookie serialization, domain rejection, bounds and expiry filtering. The Android Keystore encryption layer requires runtime/device validation.
- API route and cookie names remain undocumented by the deployed Pages handlers available to this project. This code persists cookies returned by the existing HTTPS client without assuming a particular cookie name, but it does not verify the backend contract.
- A device/emulator smoke test and server-side session-expiry check remain pending. Production write testing is not performed.
