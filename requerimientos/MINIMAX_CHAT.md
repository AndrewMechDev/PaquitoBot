# Chat MiniMax de demostración (Android)

## Activación local

1. Obtén una API key en el [panel de MiniMax](https://platform.minimax.io/).
2. Añade esta línea a `local.properties` en la raíz del proyecto (archivo ignorado por Git):

   ```properties
   minimax.apiKey=TU_API_KEY_DE_MINIMAX
   ```

3. Sincroniza Gradle, recompila la variante **debug** e inicia la app. Si falta la clave, el chat muestra un error de configuración. La antigua propiedad `gemini.apiKey` ya no se usa.

El chat usa [Chat Completions de MiniMax](https://platform.minimax.io/docs/api-reference/text-chat-openai) con `MiniMax-M3`. Envía el JSON de demostración generado desde los modelos de Inicio, Cursos, Horarios y Avisos junto con cada pregunta. Las fechas y etiquetas como «hoy» pertenecen al escenario mock; no son información actual del estudiante. El repositorio conserva los últimos seis pares de mensajes mientras la pantalla siga viva.

## Seguridad

La compilación **debug** incluye la clave en el APK para llamadas directas desde Android. Una clave incluida en un APK puede extraerse. La variante **release** compila con la clave vacía. Antes de distribuir el chat, el backend debe custodiar la clave y hacer las llamadas a MiniMax tras validar la sesión de Clerk.

No agregues la clave al repositorio ni la pegues en chats. Si se expone, revócala y crea otra.
