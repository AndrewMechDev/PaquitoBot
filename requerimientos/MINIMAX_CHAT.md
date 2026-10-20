# Chat MiniMax de demostración (Android)

## Activación local

1. Obtén una API key en el [panel de MiniMax](https://platform.minimax.io/).
2. Añade esta línea a `local.properties` en la raíz del proyecto (archivo ignorado por Git):

   ```properties
   minimax.apiKey=TU_API_KEY_DE_MINIMAX
   ```

3. Sincroniza Gradle, recompila la variante **debug** e inicia la app. Si falta la clave, el chat muestra un error de configuración. La antigua propiedad `gemini.apiKey` ya no se usa.

El chat usa [Chat Completions de MiniMax](https://platform.minimax.io/docs/api-reference/text-chat-openai) con `MiniMax-M3`. Envía el JSON de demostración generado desde los modelos de Inicio, Cursos, Horarios y Avisos junto con cada pregunta. Las fechas y etiquetas como «hoy» pertenecen al escenario mock; no son información actual del estudiante. El repositorio conserva los últimos seis pares de mensajes mientras la pantalla siga viva.

## iOS (Debug)

1. Copia `iosApp/Configuration/Config.local.template.xcconfig` como `iosApp/Configuration/Config.local.xcconfig` si todavía no existe.
2. Conserva tu `CLERK_PUBLISHABLE_KEY` y añade la clave de MiniMax:

   ```text
   MINIMAX_API_KEY = TU_API_KEY_DE_MINIMAX
   ```

3. Compila el scheme `iosApp` en configuración **Debug**. SwiftUI usará `MiniMaxChatService`, enviará el mismo contexto académico demo que Android y mostrará errores de configuración o red dentro del chat.

La clave no se guarda en el repositorio: `Config.local.xcconfig` está ignorado por Git. Si falta, iOS muestra la ruta exacta donde agregarla.

## Seguridad

La compilación **debug** incluye la clave en el APK para llamadas directas desde Android. Una clave incluida en un APK puede extraerse. La variante **release** compila con la clave vacía. Antes de distribuir el chat, el backend debe custodiar la clave y hacer las llamadas a MiniMax tras validar la sesión de Clerk.

En iOS, la llamada directa también está limitada a **Debug** para pruebas locales. La configuración **Release** fuerza `MINIMAX_API_KEY` a vacío y conserva el servicio compartido de demostración. Para producción se necesita un endpoint del backend que valide la sesión de Clerk y custodie la clave de MiniMax; `canvas-mock` no implementa ese endpoint.

No agregues la clave al repositorio ni la pegues en chats. Si se expone, revócala y crea otra.
