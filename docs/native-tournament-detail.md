## Native tournament detail screen

The tournament list now opens a dedicated Compose detail screen rather than an alert dialog.

- Back navigation returns to the list.
- The screen clearly distinguishes local-created tournaments from sample events.
- Local registration and withdrawal, share, edit and deletion confirmation are accessible from the detail view.
- No unverified API fields are invented: dates, brackets, rules, capacity, results and organizers are explicitly not presented as confirmed.
- All actions are local-device only and are not synchronized or published.

Pure presentation labels and registration state text are covered by JVM unit tests. Instrumented UI navigation tests and backend verification remain pending.
