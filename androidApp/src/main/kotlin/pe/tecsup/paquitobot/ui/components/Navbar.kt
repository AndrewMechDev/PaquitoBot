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
        // Grupo de tabs con wrapper translucido + BORDE sutil blanco.
        // Iteracion 2026-08-06 01:51: RESTAURADO el wrapper + borde blanco
        // que el usuario pidio mantener ("agregar el contorno del navbar").
        // El fondo ahora es MAS sutil (alpha 0.08 vs 0.10 anterior) y se
        // acompana de un borde 1dp rgba(255,255,255,0.4) que define
        // claramente el contorno de la pill de tabs.
        //   fondo: Color.White.copy(alpha = 0.08f)
        //   borde: 1dp @ rgba(255,255,255,0.4) = #66FFFFFF
        // Width fijo: 3 tabs * 72dp + 2 gaps * 6dp + padding 4dp * 2 = 232dp.
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.08f))
                .border(
                    width = 1.dp,
                    color = Color(0x66FFFFFF),
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

        // Boton Paquito con badge opcional encima. Circular, fondo oscuro
        // (no blanco) para que los "ojos" y "orejas" del icono Paquito
        // (que el SVG dibuja como huecos via fill-rule=evenodd) se vean
        // BLANCOS contrastando contra el FAB oscuro. Iteracion 2026-08-06
        // 01:51: fondo cambiado de PaquitoColors.Background (blanco) a
        // Color(0xFF1C1B1F) (grafito, mismo color que el texto fuerte).
        //
        // El Box contenedor mide 72dp (no 56dp) para dejar espacio al
        // badge arriba-derecha sin que se "fusione" con el FAB circular.
        //   Box contenedor 72x72dp
        //   FAB centrado 56x56dp (offset 8dp en cada lado)
        //   Badge 20x20dp en top-end (offset -2dp para asomar)
        Box(modifier = Modifier.size(width = 72.dp, height = 72.dp)) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(width = 56.dp, height = 56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        clip = false,
                    )
                    .clip(CircleShape)
                    .background(Color(0xFF1C1B1F))
                    .clickable(onClick = onPaquitoClick)
                    .padding(8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_paquito_bot),
                    contentDescription = "Abrir chat con Paquito",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }

            // Badge de notificaciones (Figma 364:222): rounded 12dp, bg #FF445A,
            // border 1.5px #FAFBFC, texto blanco Bold 10sp.
            // Iteracion 2026-08-06 01:51: el badge debe ASOMAR por encima
            // del FAB sin tocarse. Calculamos posicion dentro del Box de
            // 72dp: el FAB (56dp) centrado deja 8dp de margen a la derecha
            // del circulo. El badge (20dp) en top-end ocupa x=52..72
            // y=0..20. Para que la mitad del badge entre al FAB y la otra
            // mitad asome por fuera (efecto clasico de "notification dot"
            // sobre un icono), usamos offset(x = -6.dp, y = (-4).dp) que
            // centra el badge sobre la esquina top-end del FAB.
            if (notificationCount != null && notificationCount > 0) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = (-6).dp, y = (-4).dp)
                        .size(width = 20.dp, height = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(PaquitoColors.StateDanger)
                        .border(1.5.dp, Color(0xFFFAFBFC), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = notificationCount.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaquitoColors.TextOnWhite,
                    )
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
                    NavTab.Cursos -> R.drawable.ic_paquito_book_outline
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