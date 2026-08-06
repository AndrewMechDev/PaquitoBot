package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Tabs del Navbar (ordenados de izquierda a derecha segun Figma nodeId 333:198).
 */
enum class NavTab(val label: String) {
    Inicio("Inicio"),
    Cursos("Cursos"),
    Horarios("Horarios"),
}

/**
 * Navbar inferior compartido por las pantallas Home, Chat, Cursos, Horarios.
 *
 * Iteracion 2026-08-06:
 *   - Padding lateral eliminado del wrapper externo; el Navbar ya no usa
 *     fillMaxWidth() en el Row principal. Esto evita que el Row estire los
 *     elementos y los mueva cuando cambia el contenido (ej. badge vs sin badge).
 *     Ahora el Row ocupa solo su ancho intrinseco y se centra dentro del Box
 *     que provee el caller (HomeScreen/ChatScreen/etc).
 *   - Tamaños fijos y constantes:
 *       * tab width 72dp (no flex, no weight, no spread).
 *       * pill group gap 6dp entre tabs.
 *       * pill <-> FAB gap 12dp.
 *       * FAB container 72x72dp (para dar espacio al badge arriba).
 *       * FAB circulo 56x56dp centrado dentro del container.
 *       * Badge 20x20dp en top-end del container, offset 2dp,-2dp.
 *     Como todos los anchos son fijos, la Navbar mantiene EXACTAMENTE la misma
 *     posicion horizontal en cualquier pantalla que la renderice, siempre que
 *     el caller la centre.
 *
 * Como queda:
 *   Ancho total = 3 * 72dp (tabs) + 2 * 6dp (gaps entre tabs) + 8dp (padding
 *   pill horizontal) + 12dp (gap pill<->FAB) + 72dp (FAB container) = 320dp.
 *   En una pantalla de 360dp, sobra 20dp a cada lado (centrado).
 *   En una de 411dp, sobra 45.5dp a cada lado.
 */
@Composable
fun Navbar(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onPaquitoClick: () -> Unit,
    notificationCount: Int? = null,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 16.dp,
) {
    Row(
        // Importante: NO usamos fillMaxWidth() para que el Row ocupe solo el
        // ancho del contenido. Esto + el Box con contentAlignment=Center
        // del caller garantiza que la Navbar se vea en la misma posicion
        // horizontal en todas las pantallas (Home, Chat, Cursos, Horarios).
        modifier = modifier.padding(horizontal = horizontalPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Grupo de tabs con estilo GLASS (referencia Samsung Health, pedido
        // explicito del usuario 2026-08-06 - no viene de Figma, es una
        // mejora de estilo nueva).
        //
        // Iteracion 2026-08-06 (fix): la primera version usaba fondo y
        // borde BLANCOS translucidos, pensada para un fondo con color
        // detras (como la referencia Samsung Health, fondo rosa/violeta).
        // Pero el fondo del Home ahora es BLANCO (ver SurfaceHomeCanvas),
        // asi que blanco-sobre-blanco practicamente desaparecia sin dejar
        // ver el "vidrio". Fix: en vez de depender del contraste de color
        // de fondo, el glass se define con sombra + fondo translucido con
        // cuerpo + borde con tinte OSCURO sutil en degrade (patron "frosted
        // glass" de iOS en modo claro).
        //
        // Iteracion 2026-08-06 (suavizado): el usuario pidio "bajarle un
        // poco" a este glass sin perder que se note. Reducidos: sombra
        // 6dp->4dp, opacidad de fondo 0.55/0.35->0.42/0.24, alpha del
        // borde 0.14/0.04->0.10/0.03. El MISMO patron (sombra + fondo
        // translucido + borde oscuro en degrade) se reutiliza en el FAB de
        // Paquito, mas abajo, para mantener consistencia visual.
        // NOTA tecnica: sigue sin haber blur real (Modifier.blur no
        // difumina lo que esta DETRAS de esta capa; un backdrop blur real
        // requeriria la libreria Haze, no agregada a este proyecto).
        // Width fijo: 3 tabs * 72dp + 2 gaps * 6dp + padding 4dp * 2 = 232dp.
        Row(
            modifier = Modifier
                .shadow(
                    elevation = 4.dp,
                    shape = RoundedCornerShape(50),
                    clip = false,
                )
                .clip(RoundedCornerShape(50))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.42f),
                            Color.White.copy(alpha = 0.24f),
                        ),
                    ),
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.10f),
                            Color.Black.copy(alpha = 0.03f),
                        ),
                    ),
                    shape = RoundedCornerShape(50),
                )
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            NavTab.Inicio.let { NavTabItem(it, currentTab == it) { onTabSelected(it) } }
            NavTab.Cursos.let { NavTabItem(it, currentTab == it) { onTabSelected(it) } }
            NavTab.Horarios.let { NavTabItem(it, currentTab == it) { onTabSelected(it) } }
        }

        // Boton Paquito con badge opcional encima. Circular, GLASS OSCURO:
        // mismo patron que la pill de tabs (sombra + fondo translucido +
        // borde sutil en degrade) pero en tonos oscuros en vez de claros.
        //
        // Iteracion 2026-08-06 (fix mancha blanca, decision final): se
        // probo el FAB en glass CLARO (mismo tono que la pill) pero el
        // icono de Paquito (el SVG real de Figma) tiene espacio negativo
        // propio alrededor del cuerpo -confirmado con un screenshot del
        // nodo 351:441 aislado en Figma- que se disimula sobre fondo de
        // color/oscuro pero se ve como mancha blanca sobre fondo claro.
        // Para mantener la mejor consistencia visual SIN mostrar la mancha,
        // el FAB usa la variante OSCURA del mismo patron glass (sombra +
        // degrade translucido oscuro + borde claro sutil) en vez de negro
        // solido plano o glass claro. El icono vuelve a fill BLANCO (ver
        // ic_paquito_bot.xml) porque el fondo del FAB es oscuro de nuevo.
        //
        // Iteracion 2026-08-06 (re-extraccion "Paquito-v2", frame 351:644):
        // recalculado el tamano del FAB y la posicion del badge a partir de
        // la geometria real de Figma. El badge (23x23px, borde 3px) en
        // Figma sobresale del FAB apenas ~4px arriba y ~2px a la derecha -
        // practicamente pegado, NO flotando separado como en la iteracion
        // anterior (offset -8,-6 sobre un contenedor de 72dp/FAB 56dp
        // quedaba desproporcionado). Ahora:
        //   - FAB 64dp (antes 56dp) - proporcion correcta contra el resto
        //     del Navbar (pill de tabs) segun el ancho real del navbar en
        //     Figma (384.86px).
        //   - Contenedor externo 80dp (64 + 8dp de margen a cada lado,
        //     igual que antes) para dar lugar a la sombra del FAB.
        //   - El badge se ancla DIRECTO al Box del FAB (64dp), no al
        //     contenedor externo de 80dp, con un offset pequeno hacia
        //     afuera (no negativo/hacia adentro) que replica el "apenas
        //     asoma" real de Figma.
        Box(modifier = Modifier.size(width = 80.dp, height = 80.dp)) {
            Box(
                modifier = Modifier.align(Alignment.Center).size(width = 64.dp, height = 64.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .shadow(
                            elevation = 8.dp,
                            shape = CircleShape,
                            clip = false,
                        )
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color(0xFF2A2930).copy(alpha = 0.92f),
                                    Color(0xFF1C1B1F).copy(alpha = 0.88f),
                                ),
                            ),
                        )
                        .border(
                            width = 1.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.28f),
                                    Color.White.copy(alpha = 0.08f),
                                ),
                            ),
                            shape = CircleShape,
                        )
                        .clickable(onClick = onPaquitoClick),
                    contentAlignment = Alignment.Center,
                ) {
                    // El agrandado ~20% (55x44dp) ya no es indispensable
                    // para tapar espacio negativo (el fondo oscuro lo
                    // disimula igual que en la referencia de Figma), pero
                    // se mantiene porque da un mejor encuadre visual del
                    // icono dentro del circulo (menos margen muerto).
                    Image(
                        painter = painterResource(id = R.drawable.ic_paquito_bot),
                        contentDescription = "Abrir chat con Paquito",
                        modifier = Modifier.size(width = 55.dp, height = 44.dp),
                        contentScale = ContentScale.Fit,
                    )
                }

                // Badge de notificaciones (Figma 285:665, frame 351:644):
                // 23x23px, rounded, bg #FF445A, border 3px #FAFBFC, texto
                // blanco Bold 11sp. Anclado al borde top-end del FAB mismo
                // (no del contenedor externo), con offset pequeno hacia
                // afuera para el "apenas asoma" fiel a Figma.
                if (notificationCount != null && notificationCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .offset(x = 3.dp, y = (-3).dp)
                            .size(width = 22.dp, height = 22.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(PaquitoColors.StateDanger)
                            .border(2.5.dp, Color(0xFFFAFBFC), RoundedCornerShape(11.dp)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = notificationCount.toString(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = PaquitoColors.TextOnWhite,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NavTabItem(tab: NavTab, selected: Boolean, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .width(72.dp)
            .clip(RoundedCornerShape(if (selected) 50 else 35))
            .background(if (selected) PaquitoColors.SurfaceElevated else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(
                id = when (tab) {
                    NavTab.Inicio -> R.drawable.ic_paquito_home_outline
                    NavTab.Cursos -> R.drawable.ic_paquito_book_fill
                    NavTab.Horarios -> R.drawable.ic_paquito_calendar_fill
                }
            ),
            contentDescription = tab.label,
            modifier = Modifier.size(22.dp),
            contentScale = ContentScale.Fit,
        )
        Text(
            text = tab.label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = PaquitoColors.TextOnSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF202020)
@Composable
private fun NavbarPreview() {
    PaquitoTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF202020))
                .padding(16.dp),
        ) {
            Navbar(
                currentTab = NavTab.Inicio,
                onTabSelected = {},
                onPaquitoClick = {},
                notificationCount = 3,
            )
        }
    }
}