# Workflow de Figma → Código (guía de referencia)

> Este archivo documenta cómo pasamos diseños de Figma a código en este proyecto. Úsalo como checklist mientras trabajas, sobre todo si es tu primera vez con el MCP de Figma.

## Setup (una sola vez)

1. Archivo Figma duplicado a la cuenta educacional propia (ya hecho) — esto da acceso Full seat sin límite de 6 llamadas/mes.
2. Plugin de Figma instalado en Claude Code:
   ```
   claude plugin install figma@claude-plugins-official
   ```
3. Dentro de una sesión de Claude Code, autenticar con `/mcp` (o `/plugin` → Installed → figma → Enter). Debe aparecer `plugin:figma:figma` como conectado.
4. Verificar conexión: pídele a Claude Code algo simple como "confirma que tienes acceso al MCP de Figma" antes de empezar a extraer nada.

### Importante: la cuenta de Figma no depende de la cuenta de Claude Code

Son dos logins completamente independientes. El acceso al archivo de Figma se define en el momento de autenticar (paso 3), vía OAuth por navegador — no por ningún dato que se configure de antemano.

Al correr `/mcp`, se abre el navegador pidiendo iniciar sesión en Figma. **En esa pantalla hay que loguearse con la cuenta estudiantil** (la que tiene el archivo duplicado con Full seat), no con ninguna otra cuenta de Figma que pueda estar activa por defecto en el navegador.

Checklist antes de autenticar:
- Si el navegador ya tiene una sesión de Figma abierta con otra cuenta, cerrarla primero (o usar una ventana de incógnito) para evitar loguear la cuenta equivocada por accidente.
- Confirmar, ya autenticado, que el archivo duplicado aparece disponible (por ejemplo pidiéndole a Claude Code que liste los archivos accesibles o confirmando el nombre del archivo antes de extraer nada).
- Si en algún momento se necesita cambiar de cuenta, hay que volver a autenticar desde `/mcp` — no queda "recordada" ninguna cuenta anterior de forma automática si se cierra sesión en Figma.

## Estructura del archivo Figma en este proyecto

- **Página "Sets"**: design system — colores, tipografía, espaciados, set de íconos.
- **Página "Onboarding"**: frames del flujo de onboarding.
- **Página "Inicio"**: frames del flujo principal (home).

## Orden de trabajo recomendado

No extraigas frame por frame desde el inicio — primero la base, después las pantallas. Si generas pantallas antes de tener el tema base, vas a terminar con colores y tipografías hardcodeados por pantalla, difíciles de mantener luego.

### Paso 1 — Design system primero
1. Selecciona la página o frame de "Sets" en Figma.
2. Pide a Claude Code que use `get_variable_defs` sobre esa selección.
3. Objetivo: generar un archivo de tema compartido (ej. `Theme.kt` para Compose Multiplatform, con su equivalente de tokens para el lado SwiftUI) con los colores, tipografía y espaciados reales — nada de valores hex sueltos dentro de cada pantalla.
4. Íconos: selecciona todo el set de una sola vez y expórtalos como SVG en batch (no uno por uno). Guárdalos en la carpeta de recursos correspondiente (`commonMain/composeResources/drawable` o similar).

### Paso 2 — Onboarding
1. Frame por frame: selecciona el frame en Figma (o copia su link).
2. Pide a Claude Code que use `get_design_context` sobre ese frame específico.
3. Indica explícitamente que reutilice el `Theme.kt` generado en el Paso 1 — no que regenere colores o estilos nuevos por pantalla.
4. Revisa visualmente el resultado con `get_screenshot` antes de dar el frame por cerrado.

### Paso 3 — Inicio
1. Mismo proceso que el Paso 2.
2. Si hay componentes que se repiten entre Onboarding e Inicio (botones, cards, inputs), dile a Claude Code que los reutilice como componentes compartidos en vez de regenerarlos — evita inconsistencias pequeñas entre pantallas que deberían verse idénticas.

## Buenas prácticas para no gastar llamadas de más

- Extrae el set de íconos completo de una vez, no ícono por ícono.
- Antes de re-extraer un frame, revisa si ya tienes ese componente generado en otra pantalla.
- Usa `get_metadata` para orientarte en un frame grande antes de pedir el detalle completo con `get_design_context` — es más liviano y te ayuda a confirmar que seleccionaste lo correcto.

## Herramientas del MCP que vas a usar más seguido

| Herramienta | Para qué sirve |
|---|---|
| `get_design_context` | Extrae el frame seleccionado como código/estructura — el "caballito de batalla" para generar pantallas. |
| `get_variable_defs` | Extrae variables y estilos (colores, tipografía) de una selección — para el design system. |
| `get_metadata` | Mapa liviano de las capas de un frame grande, útil para orientarse antes de pedir el detalle completo. |
| `get_screenshot` | Captura visual de la selección — úsala para comparar el resultado generado contra el diseño original. |

## Troubleshooting rápido

- Si `/mcp` no muestra el servidor de Figma: reinicia Claude Code completo (las conexiones MCP se inicializan al arrancar).
- Si te da error de límite de llamadas a pesar de estar en tu cuenta educacional: confirma que estás trabajando sobre el archivo duplicado en tu propio team, no sobre el original de tus compañeros.
- Si el resultado no usa los colores/tipografía correctos: probablemente no le indicaste que reutilice `Theme.kt` — sé explícito en el prompt.
