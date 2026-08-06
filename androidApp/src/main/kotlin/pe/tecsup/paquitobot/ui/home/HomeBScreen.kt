package pe.tecsup.paquitobot.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.components.AlertCard
import pe.tecsup.paquitobot.ui.components.Day
import pe.tecsup.paquitobot.ui.components.DayState
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.Navbar
import pe.tecsup.paquitobot.ui.components.TaskBadge
import pe.tecsup.paquitobot.ui.components.TaskList
import pe.tecsup.paquitobot.ui.components.TaskListHeader
import pe.tecsup.paquitobot.ui.components.TaskListItem
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Datos del usuario y la semana que se renderizan en el header superior.
 *
 * Por ahora son hardcodeados; cuando llegue el backend se reciben del
 * ViewModel (siguiendo el patron MVVM + Repository de la skill
 * architecture-paquitobot).
 */
data class HomeBData(
    val greeting: String = "Hola, Andrea",
    val avatarInitials: String = "C24",
    val days: List<DayItem> = defaultWeek(),
    val taskItems: List<TaskListItem> = defaultTasks(),
    val alert: AlertData? = defaultAlert(),
    val pendingCount: Int = 3,
)

data class DayItem(
    val dayOfWeek: String,
    val dayNumber: String,
    val state: DayState,
    val selected: Boolean,
)

data class AlertData(
    val chip: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val primaryAction: String,
    val secondaryAction: String,
)

private fun defaultWeek(): List<DayItem> = listOf(
    DayItem("L", "3", DayState.WithTask, selected = true),
    DayItem("M", "4", DayState.WithTask, selected = false),
    DayItem("M", "5", DayState.Neutral,  selected = false),
    DayItem("J", "6", DayState.Critical, selected = false),
    DayItem("V", "7", DayState.WithTask, selected = false),
    DayItem("S", "8", DayState.Neutral,  selected = false),
    DayItem("D", "9", DayState.Neutral,  selected = false),
)

private fun defaultTasks(): List<TaskListItem> = listOf(
    TaskListItem("NT", TaskBadge.Info,    "Nombre :v",                    "Curso",                                       "hace cuanto"),
    TaskListItem("FO", TaskBadge.Warning, "Foro de Ética por vencer",     "Martes 20:00 · falta tu respuesta",           "2 d"),
    TaskListItem("EX", TaskBadge.Neutral, "Parcial de Álgebra Lineal",    "Jueves 08:00 · aula B-204",                   "4 d"),
)

private fun defaultAlert(): AlertData = AlertData(
    chip = "VENCE EN 6 H",
    category = "Cálculo II",
    title = "Laboratorio 4 — Integrales",
    subtitle = "Hoy 23:59 · vale 15% de la nota final",
    primaryAction = "Entregar",
    secondaryAction = "Recordar 2 h",
)

/**
 * Home B (Figma nodeId 364:219).
 *
 * Es la version recomendada del Home (cubre el JTBD #3 del MVP: pendientes
 * dispersos con countdown real). Estructura:
 *
 *   [Header: saludo + avatar]            (Container 364:275)
 *   [Fila de 7 dias del calendario]      (Container 364:285)
 *   [Lista de tareas]                    (Container 364:224 → Overlay+Border+Shadow)
 *   [Card de alerta critica]             (Container 364:225)
 *   [Navbar inferior con badge "3"]
 *
 * Mientras no hay ViewModel ni backend, los datos se pasan via [data] con
 * valores por defecto hardcoded. Cuando se conecte el backend, se reemplaza
 * por un ViewModel que consume un Repository.
 */
@Composable
fun HomeBScreen(
    data: HomeBData = HomeBData(),
    onDayClick: (Int) -> Unit = {},
    onPrimaryAction: () -> Unit = {},
    onSecondaryAction: () -> Unit = {},
    currentTab: NavTab = NavTab.Inicio,
    onTabSelected: (NavTab) -> Unit = {},
    onPaquitoClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(PaquitoColors.Background)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 110.dp), // espacio para la navbar
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // HEADER: saludo + avatar (Container 364:275).
            HomeBHeader(greeting = data.greeting, avatarInitials = data.avatarInitials)

            // FILA DE DÍAS (Container 364:285).
            HomeBDaysRow(
                days = data.days,
                onDayClick = onDayClick,
            )

            // CONTENIDO SCROLLABLE: TaskList + AlertCard.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = PaquitoSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(PaquitoSpacing.md),
            ) {
                TaskList(
                    header = TaskListHeader(
                        title = "Paquito te avisó",
                        actionLabel = "Ver todo",
                    ),
                    items = data.taskItems,
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

        // NAVBAR anclada abajo.
        // Iteracion 2026-08-06 01:37: padding lateral removido (lo maneja
        // internamente el Navbar via `horizontalPadding`) para evitar
        // doble padding y que se desfase con las demas pantallas.
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center,
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

@Composable
private fun HomeBHeader(greeting: String, avatarInitials: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PaquitoSpacing.xl,
                end = PaquitoSpacing.xl,
                top = 60.dp,
                bottom = 14.dp,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = greeting,
            style = PaquitoTypography.Greeting.copy(
                color = PaquitoColors.TextOnCardStrong,
            ),
        )
        Box(
            modifier = Modifier
                .size(62.dp)
                .clip(RoundedCornerShape(31.dp))
                .background(PaquitoColors.Background),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .shadow(elevation = 1.dp, shape = RoundedCornerShape(25.dp))
                    .clip(RoundedCornerShape(25.dp))
                    .background(PaquitoColors.Background),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = avatarInitials,
                    style = PaquitoTypography.Greeting.copy(
                        fontSize = 17.sp,
                        color = PaquitoColors.TextOnCardStrong,
                    ),
                )
            }
        }
    }
}

@Composable
private fun HomeBDaysRow(
    days: List<DayItem>,
    onDayClick: (Int) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PaquitoSpacing.xl),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        days.forEachIndexed { index, item ->
            Day(
                dayOfWeek = item.dayOfWeek,
                dayNumber = item.dayNumber,
                state = item.state,
                selected = item.selected,
                onClick = { onDayClick(index) },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeBScreenPreview() {
    PaquitoTheme {
        HomeBScreen()
    }
}