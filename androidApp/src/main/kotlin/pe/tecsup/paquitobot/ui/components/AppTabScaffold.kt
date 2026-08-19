package pe.tecsup.paquitobot.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.ui.theme.PaquitoColors

/**
 * Chrome compartido de las pestañas Inicio / Cursos / Horarios.
 * Extraido de Home para no repetir navbar + edge-to-edge en cada pantalla.
 * Figma `351:697` y `351:719` solo traen navbar; el cuerpo es de producto.
 *
 * Iteracion 2026-08-13 (fix Navbar flotante, reportado por el usuario):
 * - El contenido pasa a ser UNA sola superficie scrolleable (antes cada
 *   pantalla manejaba su propio scroll interno en secciones sueltas) -
 *   necesario para que el Navbar pueda reaccionar al scroll de la pagina
 *   completa, y para que `hazeSource` tenga una sola superficie de la cual
 *   tomar el contenido a difuminar.
 * - `hazeSource`/`hazeEffect` (libreria Haze) reemplazan el glass simulado
 *   con opacidad plana que habia antes (sin blur real, documentado como
 *   limitacion conocida en `Navbar.kt`) - ahora el Navbar difumina de
 *   verdad el contenido que tiene detras en vez de taparlo con una franja
 *   translucida plana.
 * - El Navbar se oculta (fade + slide) SOLO si el scroll supera un umbral
 *   de velocidad/delta (scroll rapido) - un scroll lento o el contenido
 *   quieto no lo ocultan. Reaparece al superar el umbral scrolleando para
 *   arriba, o automaticamente [SETTLE_DELAY_MS] despues de que el scroll
 *   se detiene.
 */
@Composable
fun AppTabScaffold(
    currentTab: NavTab,
    onTabSelected: (NavTab) -> Unit,
    onPaquitoClick: () -> Unit,
    modifier: Modifier = Modifier,
    notificationCount: Int? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val hazeState = rememberHazeState()
    var navbarVisible by remember { mutableStateOf(true) }
    var scrollEventToken by remember { mutableIntStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val hideThresholdPx = with(LocalDensity.current) { SCROLL_HIDE_THRESHOLD.toPx() }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val deltaY = available.y
                when {
                    // Scroll rapido hacia abajo (contenido subiendo) -> ocultar.
                    deltaY < -hideThresholdPx -> navbarVisible = false
                    // Scroll rapido hacia arriba -> mostrar de nuevo.
                    deltaY > hideThresholdPx -> navbarVisible = true
                }
                // Debounce: si no llega otro evento de scroll en SETTLE_DELAY_MS,
                // se asume que el usuario se detuvo y se muestra de nuevo.
                scrollEventToken++
                val myToken = scrollEventToken
                coroutineScope.launch {
                    delay(SETTLE_DELAY_MS)
                    if (myToken == scrollEventToken) navbarVisible = true
                }
                return Offset.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.SurfaceHomeCanvas)
            .systemBarsPadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .hazeSource(state = hazeState)
                .padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            content = content,
        )
        AnimatedVisibility(
            visible = navbarVisible,
            enter = fadeIn(tween(200)) + slideInVertically(tween(200)) { it / 2 },
            exit = fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 2 },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center,
            ) {
                Navbar(
                    currentTab = currentTab,
                    onTabSelected = onTabSelected,
                    onPaquitoClick = onPaquitoClick,
                    notificationCount = notificationCount,
                    hazeState = hazeState,
                )
            }
        }
    }
}

/** Delta minimo (por evento de scroll) para considerar el scroll "rapido" y ocultar el Navbar. */
private val SCROLL_HIDE_THRESHOLD = 12.dp
private const val SETTLE_DELAY_MS = 300L
