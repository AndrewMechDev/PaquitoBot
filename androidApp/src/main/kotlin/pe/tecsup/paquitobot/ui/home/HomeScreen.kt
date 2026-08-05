package pe.tecsup.paquitobot.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.components.AlertCard
import pe.tecsup.paquitobot.ui.components.DayState
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.Navbar
import pe.tecsup.paquitobot.ui.components.TaskBadge
import pe.tecsup.paquitobot.ui.components.TaskList
import pe.tecsup.paquitobot.ui.components.TaskListHeader
import pe.tecsup.paquitobot.ui.components.TaskListItem
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Pantalla Home definitiva de PaquitoBot (2026-08-05).
 *
 * Fusiona:
 *   - Visual / layout del Home A (Figma 351:644): fondo #A8B6BC, card oscura
 *     del calendario, dias en espanol, timestamp grande "12 h" con color
 *     por urgencia.
 *   - Datos / estructura del Home B (Figma 364:219): TaskList con badges NT/FO/EX
 *     y subtitulos, AlertCard de la entrega inminente.
 *
 * Mientras no llega el backend, los datos se inyectan via [HomeScreenData].
 * Cuando se conecte el Repository, [HomeScreenData] se reemplaza por un
 * ViewModel que consume la API.
 */
@Composable
fun HomeScreen(
    data: HomeScreenData = HomeScreenData.default(),
    onDayClick: (Int) -> Unit = {},
    onPrimaryAction: () -> Unit = {},
    onSecondaryAction: () -> Unit = {},
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 110.dp), // espacio para navbar
            verticalArrangement = Arrangement.spacedBy(30.dp),
        ) {
            // Header (Home A): "Bienvenido, {user}" + fecha completa.
            HomeHeader(
                greeting = data.greeting,
                dateLabel = data.dateLabel,
                modifier = Modifier.padding(horizontal = PaquitoSpacing.lg, vertical = 14.dp),
            )

            // Calendario semanal (Home A) + Lista de tareas (Home B).
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaquitoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(25.dp),
            ) {
                WeekCalendar(
                    title = data.weekTitle,
                    days = data.days,
                    onDayClick = onDayClick,
                )

                TaskList(
                    header = TaskListHeader(
                        title = data.tasksHeader,
                        actionLabel = data.tasksActionLabel,
                    ),
                    items = data.tasks,
                )

                if (data.alert != null) {
                    AlertCard(
                        chip = data.alert.chip,
                        category = data.alert.category,
                        title = data.alert.title,
                        subtitle = data.alert.subtitle,
                        primaryActionLabel = data.alert.primaryAction,
                        secondaryActionLabel = data.alert.secondaryAction,
                        onPrimaryAction = onPrimaryAction,
                        onSecondaryAction = onSecondaryAction,
                    )
                }
            }
        }

        // Navbar inferior (misma que Home B).
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
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

/**
 * Header de Home A: saludo + fecha completa en espanol.
 */
@Composable
private fun HomeHeader(greeting: String, dateLabel: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = greeting,
            fontSize = 32.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
        )
        Text(
            text = dateLabel,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeMuted,
        )
    }
}

/**
 * Calendario semanal en card oscura (de Home A).
 *
 * Cada dia es una columna vertical: label espanol completo arriba, numero
 * gigante 48sp abajo, color del numero segun urgencia:
 *   - dia seleccionado/destacado: blanco
 *   - normal: cyan translucido (BrandPrimary @ 70%)
 *   - critico: rojo translucido
 *
 * El dia actual (selected) usa bg gris claro translucido sobre la card
 * oscura para destacarse.
 */
@Composable
private fun WeekCalendar(
    title: String,
    days: List<WeekDayData>,
    onDayClick: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
            modifier = Modifier.padding(start = 0.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(35.dp))
                .background(PaquitoColors.SurfaceHomeWeekBg)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            days.forEachIndexed { index, day ->
                WeekDayCell(
                    day = day,
                    onClick = { onDayClick(index) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeekDayCell(
    day: WeekDayData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellBg = if (day.selected) {
        androidx.compose.ui.graphics.Color(0x47D3D3D3) // rgba(211,211,211,0.28)
    } else {
        androidx.compose.ui.graphics.Color(0xE6FFFFFF) // rgba(255,255,255,0.9)
    }
    val labelColor = if (day.selected) PaquitoColors.TextHomeDayActiveLabel else PaquitoColors.TextHomeDayLabel
    val numberColor = when (day.state) {
        DayState.Critical -> PaquitoColors.TextHomeDayNumberCritical
        else -> PaquitoColors.TextHomeDayNumber
    }
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(25.dp))
            .background(cellBg)
            .padding(vertical = 15.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = day.dayOfWeek,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = labelColor,
            maxLines = 1,
        )
        Text(
            text = day.dayNumber,
            fontSize = 32.sp, // Figma usa 48sp pero 32 esta mejor legible a la par del nav
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = numberColor,
            maxLines = 1,
        )
    }
}

/* -----------------------------------------------------------
 * Modelos de datos
 * ----------------------------------------------------------- */

/** Datos de un dia del calendario semanal. */
data class WeekDayData(
    val dayOfWeek: String, // "Lunes", "Martes", ...
    val dayNumber: String, // "03", "04", ...
    val state: DayState,
    val selected: Boolean,
)

/** Datos de la entrega critica (de Home B). */
data class AlertEntry(
    val chip: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val primaryAction: String,
    val secondaryAction: String,
)

/** Snapshot de la pantalla Home. Vendra del Repository cuando este listo. */
data class HomeScreenData(
    val greeting: String,
    val dateLabel: String,
    val weekTitle: String,
    val days: List<WeekDayData>,
    val tasksHeader: String,
    val tasksActionLabel: String?,
    val tasks: List<TaskListItem>,
    val alert: AlertEntry?,
    val pendingCount: Int,
) {
    companion object {
        fun default(): HomeScreenData = HomeScreenData(
            greeting = "Bienvenido, Andrea!",
            dateLabel = "Lunes, 5 de enero de 2026",
            weekTitle = "Semana 10",
            days = listOf(
                WeekDayData("Lunes",    "03", DayState.WithTask, selected = true),
                WeekDayData("Martes",   "04", DayState.WithTask, selected = false),
                WeekDayData("Miercoles","05", DayState.Neutral,  selected = false),
                WeekDayData("Jueves",   "06", DayState.Critical, selected = false),
                WeekDayData("Viernes",  "07", DayState.WithTask, selected = false),
                WeekDayData("Sabado",   "08", DayState.Critical, selected = false),
            ),
            tasksHeader = "Tareas Pendientes",
            tasksActionLabel = null,
            tasks = listOf(
                TaskListItem("NT", TaskBadge.Info,    "Foro de novedades",         "Curso de Narrativa",                "hace 1 h"),
                TaskListItem("FO", TaskBadge.Warning, "Foro de Etica por vencer",  "Martes 20:00 - falta tu respuesta", "2 d"),
                TaskListItem("EX", TaskBadge.Neutral, "Parcial de Algebra Lineal", "Jueves 08:00 - aula B-204",         "4 d"),
            ),
            alert = AlertEntry(
                chip = "VENCE EN 6 H",
                category = "Calculo II",
                title = "Laboratorio 4 - Integrales",
                subtitle = "Hoy 23:59 - vale 15% de la nota final",
                primaryAction = "Entregar",
                secondaryAction = "Recordar 2 h",
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