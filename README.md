# Academia Alius X — Android nativo

Aplicación Android nativa Kotlin + Jetpack Compose. No es una WebView.

## Funcionalidad nativa disponible en esta iteración

- Navegación Compose de cuatro secciones: Inicio, Torneos, Comunidad y Perfil.
- Perfil local: edición de nombre visible y selección de juegos favoritos; persistencia en SharedPreferences.
- Torneos locales/de muestra: búsqueda, filtros, detalles, inscripción/retirada local, creación, edición, eliminación y compartir.
- Copia de datos JSON: exportación e importación validada de perfil, juegos, inscripciones y torneos creados.
- Mensajes explícitos para diferenciar datos locales/de muestra de la información online. Las inscripciones locales no se publican ni se envían al servidor.
- Acceso al sitio oficial desde los espacios cuya integración nativa aún no está verificada.

## Integración online y seguridad

Los contratos de API, autenticación, autorización y escritura siguen sin estar verificados contra los handlers desplegados. No se simulan datos de comunidad ni se ejecutan mutaciones online desde estas pantallas. Consulta [docs/backend-read-only-audit.md](docs/backend-read-only-audit.md) y el [checklist de paridad](docs/native-parity-checklist.md).

La persistencia local de esta iteración utiliza preferencias de Android. El contenido local no equivale a sincronización, ni a una cuenta online. Importar una copia sustituye los datos locales actuales.

## Build y CI

GitHub Actions ejecuta tests JVM, build debug, verificación de firma y archivo APK. La verificación de CI no sustituye pruebas en dispositivo/emulador ni la verificación de contratos backend.

