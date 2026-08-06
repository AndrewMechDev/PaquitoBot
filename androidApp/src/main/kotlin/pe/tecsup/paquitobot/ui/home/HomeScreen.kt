package pe.tecsup.paquitobot.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.Navbar
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Pantalla Home de PaquitoBot, fiel al frame Figma `351:644` del archivo
 * "Paquito (copia)" (canvas Main `109:97`), re-extraído el 2026-08-05.
 *
 * Estructura (de arriba hacia abajo):
 *   1. Header / saludo     ("¡Bienvenido, {nombre}!" + fecha completa)
 *   2. Calendario semanal  ("Semana 10" + card oscura con 6 dias en grid 3x2)
 *   3. Tareas pendientes   ("Tareas Pendientes" + lista con timestamps grandes)
 *   4. Navbar inferior     (3 tabs + boton Paquito + badge "3")
 *
 * El dia actual se distingue por tener fondo gris translucido (en vez de
 * blanco), igual que en Figma. No usa semaforo de colores; la urgencia se
 * codifica en el color del numero (rojo translucido para critico) y en el
 * timestamp grande ("12 h" en colores cyan para urgente, mas claro para futuro).
 *
 * Mientras no llega el backend, los datos se inyectan via [HomeScreenData].
 */
@Composable
fun HomeScreen(
    data: HomeScreenData = HomeScreenData.default(),
    currentTab: NavTab = NavTab.Inicio,
    onTabSelected: (NavTab) -> Unit = {},
    onPaquitoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.SurfaceHomeCanvas),
    ) {
        // Cuerpo principal: padding-left 25dp, top 50dp, width 380dp (Figma).
        // Compactado para que el contenido (header + semana + 4 tareas) entre
        // en pantallas estandar sin necesidad de scrollear.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 20.dp, top = 50.dp, end = 20.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 1. Saludo + fecha.
            HomeMessage(
                greeting = data.greeting,
                dateLabel = data.dateLabel,
            )

            // 2 + 3. Calendario semanal + tareas pendientes.
            // La lista de tareas usa scroll vertical interno (sin barra visible)
            // para que cualquier numero de items sea accesible en pantallas
            // pequenas. El calendario queda estatico encima.
            Column(
                verticalArrangement = Arrangement.spacedBy(18.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                HomeWeekSection(
                    title = data.weekTitle,
                    days = data.days,
                )

                HomeTasksSection(
                    title = data.tasksHeader,
                    items = data.tasks,
                )
            }
        }

        // 4. Navbar inferior (centrada horizontal, pegada abajo).
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 18.dp, start = 12.dp, end = 12.dp),
        ) {
            Navbar(
                currentTab = currentTab,
                onTabSelected = onTabSelected,
                onPaquitoClick = onPaquitoClick,
                notificationCount = data.pendingCount,
            )
        }
    }
}

/* -----------------------------------------------------------
 * 1. Header: saludo + fecha completa
 * ----------------------------------------------------------- */

@Composable
private fun HomeMessage(greeting: String, dateLabel: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(
            text = greeting,
            fontSize = 26.sp,
            lineHeight = 30.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
        )
        Text(
            text = dateLabel,
            fontSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeMuted,
        )
    }
}

/* -----------------------------------------------------------
 * 2. Semana: titulo + card oscura con grid 3x2 de dias
 * ----------------------------------------------------------- */

@Composable
private fun HomeWeekSection(title: String, days: List<WeekDayData>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
        )

        // Card oscura `SurfaceHomeWeekBg` con los 6 dias en 2 filas de 3.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(190.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(PaquitoColors.SurfaceHomeWeekBg)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Distribuimos los dias en 2 filas de 3.
            days.chunked(3).forEach { rowDays ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    rowDays.forEach { day ->
                        WeekDayCell(day = day, modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Celda de un dia en el calendario semanal del Home A.
 *
 * Variantes visuales (sacadas de Figma 351:644):
 *   - dia actual (selected=true):  bg rgba(211,211,211,0.28), label blanco translucido.
 *   - dia normal (selected=false): bg rgba(255,255,255,0.9), label gris translucido.
 *   - numero: 36sp (compactado para caber en el cell); color rojo translucido
 *     si es critico, cyan translucido en otro caso.
 *
 * El dia se trunca a 7 caracteres con ellipsis para que "Miercoles" quepa en
 * el card chico sin desbordar. La altura del cell usa `defaultMinSize(minHeight=83dp)`
 * para que crezca si el contenido lo necesita (el lineHeight del numero es 40sp).
 */
@Composable
private fun WeekDayCell(day: WeekDayData, modifier: Modifier = Modifier) {
    val cellBg = if (day.selected) {
        androidx.compose.ui.graphics.Color(0x47D3D3D3) // rgba(211,211,211,0.28)
    } else {
        androidx.compose.ui.graphics.Color(0xE6FFFFFF) // rgba(255,255,255,0.9)
    }
    val labelColor = if (day.selected) {
        PaquitoColors.TextHomeDayActiveLabel
    } else {
        PaquitoColors.TextHomeDayLabel
    }
    val numberColor = if (day.isCritical) {
        PaquitoColors.TextHomeDayNumberCritical
    } else {
        PaquitoColors.TextHomeDayNumber
    }
    // Cell compacto: la altura del cell en la card de 190dp con 2 filas + padding
    // da ~85dp por fila; usamos defaultMinSize 72dp y dejamos que crezca si hace
    // falta. Fuentes mas chicas (label 12sp, numero 28sp) para compactar.
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(cellBg)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.Start,
    ) {
        Text(
            text = day.dayOfWeek,
            fontSize = 12.sp,
            lineHeight = 14.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = day.dayNumber,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = numberColor,
            maxLines = 1,
        )
    }
}

/* -----------------------------------------------------------
 * 3. Tareas pendientes: titulo + card translucida con lista
 * ----------------------------------------------------------- */

@Composable
private fun HomeTasksSection(title: String, items: List<TaskEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
        )

        // Card wrapper translucida con la lista adentro.
        // La lista interna usa scroll vertical sin barra visible: cuando hay mas
        // items de los que caben en pantalla, el usuario puede deslizar.
        val taskScroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 80.dp)
                .heightIn(max = 280.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(PaquitoColors.SurfaceHomeTaskListBg)
                .verticalScroll(taskScroll)
                .padding(8.dp),
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    TaskDivider()
                }
                TaskInfoRow(item = item)
            }
        }
    }
}

/**
 * Divider horizontal de 1dp con opacidad 50% sobre #1C1B1F, fiel al asset
 * SVG `535d4587-4140-4f80-8565-f69031d6d538.svg` del frame original.
 */
@Composable
private fun TaskDivider() {
    HorizontalDivider(
        thickness = 1.dp,
        color = androidx.compose.ui.graphics.Color(0x801C1B1F), // rgba(28,27,31,0.5)
    )
}

/**
 * Fila de tarea del Home A.
 *
 * Layout horizontal (Figma 365:896):
 *   [icono 24x24] [col (label "Curso" + titulo "Nombre Tarea")] [timestamp grande 40sp]
 *
 * El timestamp cambia de color segun la urgencia:
 *   - normal:  TextTimestampLarge      #29617B
 *   - urgente: TextTimestampLargeAccent  rgba(34,204,255,0.7)
 *   - futuro:  TextTimestampLargeMuted   rgba(0,201,251,0.5)
 *
 * Layout en Compose:
 *   - El bloque izquierdo usa `weight(1f)` para tomar todo el ancho disponible
 *     (excepto el timestamp) en vez de un width fijo. Asi el titulo puede usar
 *     el espacio real y el timestamp queda alineado a la derecha sin overflow.
 *   - `softWrap = false` en el timestamp evita que "12 h" se parta en dos lineas.
 */
@Composable
private fun TaskInfoRow(item: TaskEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = item.label,
                fontSize = 12.sp,
                lineHeight = 14.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeTaskLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.title,
                fontSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.timestamp,
            fontSize = 28.sp,
            lineHeight = 32.sp,
            softWrap = false,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = when (item.urgency) {
                TaskUrgency.Normal  -> PaquitoColors.TextTimestampLarge
                TaskUrgency.Urgent  -> PaquitoColors.TextTimestampLargeAccent
                TaskUrgency.Future  -> PaquitoColors.TextTimestampLargeMuted
            },
        )
    }
}

/* -----------------------------------------------------------
 * Modelos de datos
 * ----------------------------------------------------------- */

/** Datos de un dia del calendario semanal. */
data class WeekDayData(
    val dayOfWeek: String, // "Lunes", "Martes", "Miercoles", ...
    val dayNumber: String, // "03", "04", ...
    val selected: Boolean,
    val isCritical: Boolean,
)

/** Urgencia de una tarea, determina el color del timestamp grande. */
enum class TaskUrgency { Normal, Urgent, Future }

/** Item individual de la lista de tareas. */
data class TaskEntry(
    val label: String,    // "Curso"
    val title: String,    // "Nombre Tarea"
    val timestamp: String, // "12 h"
    val urgency: TaskUrgency,
    @androidx.annotation.DrawableRes val iconRes: Int,
)

/** Snapshot de la pantalla Home. Vendra del Repository cuando este listo. */
data class HomeScreenData(
    val greeting: String,
    val dateLabel: String,
    val weekTitle: String,
    val days: List<WeekDayData>,
    val tasksHeader: String,
    val tasks: List<TaskEntry>,
    val pendingCount: Int,
) {
    companion object {
        fun default(): HomeScreenData = HomeScreenData(
            greeting = "¡Bienvenido, {user}!",
            dateLabel = "Lunes, 5 de enero de 2026",
            weekTitle = "Semana 10",
            days = listOf(
                WeekDayData("Lunes",     "03", selected = true,  isCritical = false),
                WeekDayData("Martes",    "04", selected = false, isCritical = false),
                WeekDayData("Miercoles", "05", selected = false, isCritical = false),
                WeekDayData("Jueves",    "06", selected = false, isCritical = false),
                WeekDayData("Viernes",   "07", selected = false, isCritical = false),
                WeekDayData("Sabado",    "08", selected = false, isCritical = true),
            ),
            tasksHeader = "Tareas Pendientes",
            tasks = listOf(
                TaskEntry(
                    label = "Curso",
                    title = "Nombre Tarea",
                    timestamp = "12 h",
                    urgency = TaskUrgency.Normal,
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
                TaskEntry(
                    label = "Curso",
                    title = "Nombre Tarea",
                    timestamp = "12 h",
                    urgency = TaskUrgency.Urgent,
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
                TaskEntry(
                    label = "Curso",
                    title = "Nombre Tarea",
                    timestamp = "12 h",
                    urgency = TaskUrgency.Urgent,
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
                TaskEntry(
                    label = "Curso",
                    title = "Nombre Tarea",
                    timestamp = "12 h",
                    urgency = TaskUrgency.Future,
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
            ),
            pendingCount = 3,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreview() {
    PaquitoTheme {
        HomeScreen()
    }
}
