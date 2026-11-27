# Compose UI test coverage

Instrumented tests run against an Android emulator through GitHub Actions.

- Bottom navigation between Inicio, Comunidad and Perfil.
- Tournament detail open/back and local registration toggle; detail ID must render its actual value.
- Creating a local tournament and confirming its deletion.
- Editing and saving the local profile name.

The tests clear the app's local preferences and recreate MainActivity before each case. They use local/sample data only; no backend or production writes are involved.

CI uses API 35 Google APIs emulator and `gradle connectedDebugAndroidTest`. A successful CI run is the verification gate for these tests.