## Native tournament detail screen

The tournament list now opens a dedicated Compose detail screen rather than an alert dialog.

- Back navigation returns to the list.
- The screen clearly distinguishes local-created tournaments from sample events.
- Local registration and withdrawal, share, edit and deletion confirmation are accessible from the detail view.
- No unverified API fields are invented: dates, brackets, rules, capacity, results and organizers are explicitly not presented as confirmed.
- All actions are local-device only and are not synchronized or published.

Presentation labels and registration state are covered by JVM unit tests. Instrumented tests now exercise bottom-tab navigation, tournament-detail open/back and local registration toggle, local tournament create/delete confirmation, and profile editing. CI emulator execution is required before marking these flows verified. Backend verification remains pending.
