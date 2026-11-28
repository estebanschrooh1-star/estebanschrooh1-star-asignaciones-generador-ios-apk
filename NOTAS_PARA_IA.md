# Notas para quien modifique este proyecto (persona o IA)

App "Asignaciones": web (PWA) + Android (Capacitor). Todo el código de la app está en `www/index.html`.

## Versiones (automáticas)
- La versión es `<VERSION>.<n.º de commits>` (ej. `2.12.58`) y **siempre de 3 números**: la app solo compara 3.
- El archivo `VERSION` (2 números, ej. `2.12`) se sube a mano solo para un cambio grande. El tercer número sube solo con cada commit.
- GitHub Actions sella la versión y la fecha en `www/index.html` (`APP_VER`, `APP_DATE`, vía `scripts/prepare-web.js`)
  y en `versionName`/`versionCode` del APK. No hace falta editar `APP_VER` ni `package.json` a mano.
- Si cambias archivos de la app web, sube el número de caché en `www/sw.js` (`asig-vNN`) si quieres forzar el refresco en iPhone.

## Cómo funcionan las actualizaciones (Android)
- Al abrir la app consulta `https://api.github.com/repos/<UPDATE_REPO>/releases/latest` y compara la etiqueta (`vX.Y.Z`)
  con `APP_VER`. Si es mayor, muestra la franja "Hay una actualización pendiente" y una fila en Configuración.
- Para que exista un Release: Actions → Compilar APK y publicar PWA → Run workflow → marcar "Publicar".
  Sin Release nuevo, la app no tiene nada que detectar.
- La descarga/instalación la hace el plugin nativo `native/android/AppUpdatePlugin.java`.
- En Configuración hay una tarjeta "Actualizaciones" con el botón **Buscar actualizaciones** (solo en Android).
- **Firma fija**: `scripts/android-native.js` obliga a firmar con `resources/signing/debug.keystore` y el workflow verifica
  la huella del APK (paso "Verificar la firma del APK"). Android no instala un APK encima de otro con distinta firma
  ("package conflicts with an existing package"); el plugin lo detecta antes de abrir el instalador y la app avisa.
  Nunca cambiar ni borrar ese keystore.

## No cambiar
- `resources/signing/debug.keystore`: la firma fija permite instalar actualizaciones encima de la app anterior.
- El repositorio debe seguir siendo público (la app lee los Releases sin iniciar sesión).
- El texto de los menús no se puede seleccionar (a propósito).
- La app gira (vertical y horizontal): en horizontal/monitores se agrega la clase `ls` a `<html>`. El visor PDF en pantalla dividida queda a la derecha (45 %) y el menú a la izquierda; en vertical sigue arriba (1/3). Los bloqueos de rotación se quitaron de `manifest.webmanifest`, `scripts/android-native.js` (`fullUser`) y del JS.
- Selector de semana del PDF: tarjeta con la semana sugerida (la que empieza el próximo lunes), semanas pasadas en gris y aviso si no es la próxima. Cada semana guarda su inicio (`st`); los PDF guardados antes se calculan desde el jueves (`th`).
- El color de la app es siempre azul (se quitó el selector de colores). Al abrir Configuración se oculta el splitscreen y se vuelve a abrir al cerrarla.
