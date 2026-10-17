# Estado de integración

Por ahora no hay backend propio. La única conexión en línea de Android es Clerk, que gestiona el inicio de sesión. La app no envía solicitudes a Canvas ni a un servidor de PaquitoBot.

## Flujo actual

1. El usuario completa Welcome, Notificaciones y «Así te ayudo».
2. «Iniciar sesión con Clerk» abre `ClerkAuthGate`, que presenta `AuthView`.
3. Al autenticarse, `SessionViewModel` solo permite el acceso si el correo principal de Clerk está verificado y pertenece exactamente a `@tecsup.edu.pe`. Una sesión de otro dominio queda bloqueada y ofrece cerrar sesión para elegir otra cuenta.
4. Con el correo autorizado, se muestran Inicio, Cursos, Horarios y Chat.

Para impedir también el registro de cuentas externas en Clerk, configurar en el Dashboard de la instancia correspondiente **Access mode → Allowlist** con el dominio `tecsup.edu.pe` como único identificador permitido. Esta configuración de Clerk requiere un plan de pago en producción; el filtro de Android protege la navegación de esta app, pero no sustituye la validación del token y del dominio en un futuro backend.
4. Inicio, Cursos, Horarios y el resto de pantallas usan datos locales de ejemplo. `MockChatRepository` responde a preguntas comunes sin red.

La bandera de onboarding se guarda localmente en `SecureTokenStore`. Los datos de ejemplo no representan notas, horarios ni tareas reales de la cuenta de Clerk.

## Próxima integración

Cuando exista el nuevo backend, añadir una implementación remota por repositorio y seleccionar explícitamente qué entorno la usa. Mantener Clerk como proveedor de autenticación y definir en el nuevo backend la validación de sus tokens. No reutilizar endpoints, tokens de Canvas ni contratos del servicio anterior.
