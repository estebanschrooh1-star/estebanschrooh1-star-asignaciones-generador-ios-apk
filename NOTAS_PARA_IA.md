# Notas para quien modifique este proyecto (persona o IA)

App "Asignaciones": web (PWA) + Android (Capacitor). Todo el código de la app está en `www/index.html`.

## Al publicar CUALQUIER cambio, sube la versión en 3 lugares
1. `package.json` → `"version"` (ej. `2.11.1`).
2. `www/index.html` → `const APP_VER="2.11.1",APP_DATE="dd/mm/aaaa"`. **Debe ser igual a `package.json`**: GitHub Actions
   falla a propósito si no coinciden (la app avisaría de actualización para siempre).
3. `www/sw.js` → sube el número del nombre de caché (`asig-vNN`) para que la versión web se refresque.

## Cómo funcionan las actualizaciones (Android)
- Al abrir la app consulta `https://api.github.com/repos/<UPDATE_REPO>/releases/latest` y compara la etiqueta (`vX.Y.Z`)
  con `APP_VER`. Si es mayor, muestra la franja "Hay una actualización pendiente" y una fila en Configuración.
- El workflow `.github/workflows/android.yml` compila el APK y lo publica en Releases como `Asignaciones.apk`.
- La descarga/instalación la hace el plugin nativo `native/android/AppUpdatePlugin.java`.

## No cambiar
- `resources/signing/debug.keystore`: la firma fija permite instalar actualizaciones encima de la app anterior.
- El repositorio debe seguir siendo público (la app lee los Releases sin iniciar sesión).
- La rotación de pantalla está bloqueada en vertical y el texto de los menús no se puede seleccionar (a propósito).
