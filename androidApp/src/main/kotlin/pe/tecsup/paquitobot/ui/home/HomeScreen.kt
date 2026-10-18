package pe.tecsup.paquitobot.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.components.AcademicErrorBanner
import pe.tecsup.paquitobot.ui.components.AppTabScaffold
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.NotificationBellButton
import pe.tecsup.paquitobot.ui.events.EventMockData
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.paquitoSurface

/**
 * Pantalla Home de PaquitoBot, fiel al frame Figma `351:644` del archivo
 * "Paquito (copia)" (canvas Main `109:97`).
 *
 * Estructura (de arriba hacia abajo):
 *   1. Header / saludo     ("¡Bienvenido, {nombre}!" + fecha completa) — alineado a izquierda
 *   2. Snapshot academico de 3 cards (ranking general / ranking por ciclo / carrera)
 *   3. Calendario semanal  (7 dias en una tarjeta clara)
 *   4. Tareas pendientes   (lista unificada: foros, labs, practicas)
 *   5. Navbar inferior     (3 tabs + boton Paquito + badge)
 *
 * Iteracion 2026-08-06 (auditoria post-fusion):
 *   - Saludo re-alineado a la izquierda (antes estaba centrado y se veia "ape-
 *     gado al borde"; ahora se alinea a Start dentro del padding lateral).
 *   - Padding lateral del body: 20dp -> 24dp para que el contenido respire
 *     igual a izquierda y derecha en cualquier pantalla 360-411dp.
 *   - Card de la semana: 7 dias (incluyendo Domingo) en UNA sola fila, no un
 *     grid 3x3 (el grid dejaba el Domingo huerfano en una tercera fila
 *     descentrada). Dias abreviados a 2 letras (Lu, Ma, Mi...) para que
 *     entren las 7 celdas sin desbordar.
 *   - Item de tarea compactado (padding 10/6dp, icono 18dp, label 11sp,
 *     titulo 14sp, timestamp 24sp) para que el item no se sienta "ancho".
 *   - Icono foro eliminado del costado de "Tareas Pendientes": el frame
 *     Figma actual (re-extraido de "Paquito-v2") ya no lo tiene.
 *   - Navbar centrada horizontalmente con padding lateral 16dp en vez de 12dp
 *     para que no se vea pegada a la izquierda.
 *
 * Mientras no llega el backend, los datos se inyectan via [HomeScreenData].
 */
@Composable
fun HomeScreen(
    data: HomeScreenData = HomeScreenData.default(),
    currentTab: NavTab = NavTab.Inicio,
    onTabSelected: (NavTab) -> Unit = {},
    onPaquitoClick: () -> Unit = {},
    onNotificationsClick: () -> Unit = {},
    onEventsClick: () -> Unit = {},
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    AppTabScaffold(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        onPaquitoClick = onPaquitoClick,
        notificationCount = data.pendingCount,
        modifier = modifier,
    ) {
        HomeTabContent(
            data = data,
            onNotificationsClick = onNotificationsClick,
            onEventsClick = onEventsClick,
            errorMessage = errorMessage,
            onRetry = onRetry,
        )
    }
}

/** Contenido de Inicio sin su scaffold, para que la navegación persista fuera de la transición. */
@Composable
fun HomeTabContent(
    data: HomeScreenData,
    onNotificationsClick: () -> Unit,
    onEventsClick: () -> Unit,
    errorMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedDayNumber by remember(data) { mutableStateOf<String?>(null) }
    val selectedDay = data.days.firstOrNull { it.dayNumber == selectedDayNumber }
    val tasksForSelectedDay = selectedDay?.let { day ->
        data.tasks.filter { it.dateKey == day.dateKey }
    } ?: data.tasks
    var selectedCycleIndex by remember(data) { mutableStateOf(data.cycleRankings.lastIndex) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (errorMessage != null) {
            AcademicErrorBanner(message = errorMessage, onRetry = onRetry)
        }
        HomeMessage(
            greeting = data.greeting,
            dateLabel = data.dateLabel,
            unreadNotificationsCount = data.unreadNotificationsCount,
            onNotificationsClick = onNotificationsClick,
        )
        Text("Datos académicos de demostración", style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
        PainSnapshotRow(
            overallRanking = data.overallRanking,
            careerCode = data.careerCode,
            currentCycle = data.currentCycle,
            cycleRankings = data.cycleRankings,
            selectedCycleIndex = selectedCycleIndex,
            onCycleClick = {
                if (data.cycleRankings.isNotEmpty()) {
                    selectedCycleIndex = (selectedCycleIndex - 1).let {
                        if (it < 0) data.cycleRankings.lastIndex else it
                    }
                }
            },
        )
        HomeHighlights(
            importantNotices = data.importantNotices,
            importantNoticeCount = data.importantNoticeCount,
            onEventsClick = onEventsClick,
        )
        HomeWeekSection(
            title = data.weekTitle,
            days = data.days,
            selectedDayNumber = selectedDayNumber,
            onDaySelected = { selectedDayNumber = it },
        )
        HomeTasksSection(
            title = data.tasksHeader,
            dateLabel = selectedDay?.dateLabel,
            items = tasksForSelectedDay,
            onShowAll = { selectedDayNumber = null },
        )
    }
}

/** Tarjetas compactas de prioridad para consultar lo urgente sin abrir otras pestañas. */
@Composable
private fun HomeHighlights(
    importantNotices: List<HomeFocusItem>,
    importantNoticeCount: Int,
    onEventsClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HomeHighlightCard(
            title = "Próximos eventos",
            count = EventMockData.events.size,
            items = EventMockData.events.take(2).map { event ->
                HomeFocusItem(event.title, "${event.dateLabel} · ${event.venue}")
            },
            accent = PaquitoColors.StateInfoStrong,
            actionLabel = "Ver todos",
            onClick = onEventsClick,
        )
        HomeHighlightCard("Avisos importantes", importantNoticeCount, importantNotices, PaquitoColors.StateDanger)
    }
}

@Composable
private fun HomeHighlightCard(
    title: String,
    count: Int,
    items: List<HomeFocusItem>,
    accent: Color,
    actionLabel: String? = null,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .width(252.dp)
            .heightIn(min = 172.dp)
            .paquitoSurface()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = PaquitoTypography.TaskTitle,
                color = PaquitoColors.TextOnCardStrong,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = actionLabel ?: count.toString(),
                style = PaquitoTypography.TaskTitle,
                color = if (actionLabel != null) PaquitoColors.TextLink else accent,
            )
        }
        HorizontalDivider(color = PaquitoColors.BorderDefault)
        if (items.isEmpty()) {
            Text(
                text = "Todo al día por ahora",
                style = PaquitoTypography.BodyMedium,
                color = PaquitoColors.TextOnCardMuted,
            )
        } else {
            items.take(2).forEach { item ->
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = item.title,
                        style = PaquitoTypography.BodyMedium,
                        color = PaquitoColors.TextOnCardStrong,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = item.detail,
                        style = PaquitoTypography.Caption,
                        color = PaquitoColors.TextOnCardMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

/**
 * Iteracion 2026-08-19 (repensado, pedido explicito del usuario): las 3
 * cards ya NO muestran promedio/faltas/entregas (esos viven en Cursos y
 * Horarios) sino informacion academica de "ranking": ranking general
 * acumulado (todos los ciclos antes del actual), ranking del ciclo anterior
 * al que cursas (seleccionable entre los ciclos ya finalizados, 1° a 6°), y
 * codigo de carrera + ciclo actual.
 *
 * ⚠️ DATO DEMO, no viene del JSON academico local: ese archivo no tiene
 * NINGUN concepto de "ranking", "ciclo" ni "codigo de carrera" (tampoco lo
 * tenia el backend anterior). Se mantienen datos fijos de demo, documentados
 * como tal, mismo criterio que `ASSUMED_ABSENCE_LIMIT` en los ViewModels
 * academicos.
 */
@Composable
private fun PainSnapshotRow(
    overallRanking: Double,
    careerCode: String,
    currentCycle: Int,
    cycleRankings: List<CycleRanking>,
    selectedCycleIndex: Int,
    onCycleClick: () -> Unit,
) {
    val selectedCycle = cycleRankings.getOrNull(selectedCycleIndex)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SnapshotChip(
            label = "Ranking",
            value = "%.1f".format(overallRanking),
            hint = "de 20",
            onClick = null,
            modifier = Modifier.weight(1f),
        )
        SnapshotChip(
            label = "Ranking ciclo",
            value = selectedCycle?.let { "%.1f".format(it.ranking) } ?: "—",
            hint = selectedCycle?.let { "${it.cycle}° ciclo · toca para cambiar" } ?: "sin ciclos previos",
            onClick = if (cycleRankings.isNotEmpty()) onCycleClick else null,
            modifier = Modifier.weight(1f),
        )
        SnapshotChip(
            label = "Carrera",
            value = careerCode,
            hint = "${currentCycle}° ciclo actual",
            onClick = null,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SnapshotChip(
    label: String,
    value: String,
    hint: String,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
    valueColor: androidx.compose.ui.graphics.Color = PaquitoColors.TextHomeStrong,
) {
    Column(
        modifier = modifier
            .paquitoSurface()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeTaskLabel,
            maxLines = 1,
        )
        Text(
            text = value,
            fontSize = 21.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = valueColor,
            maxLines = 1,
        )
        Text(
            text = hint,
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextOnCardMuted,
            maxLines = 2,
        )
    }
}

/* -----------------------------------------------------------
 * 1. Header: saludo + fecha completa (izquierda) + campana (derecha)
 * ----------------------------------------------------------- */

/**
 * Iteracion 2026-08-13 (campana de notificaciones): el saludo pasa de
 * `Column` sola a `Row` con [NotificationBellButton] a la derecha - unico
 * header real de la app (Cursos/Horarios no tienen uno propio), por eso es
 * el lugar natural para la campana. Sin frame de Figma para esto; sigue el
 * patron top-right estandar.
 */
@Composable
private fun HomeMessage(
    greeting: String,
    dateLabel: String,
    unreadNotificationsCount: Int,
    onNotificationsClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Spacer(Modifier.weight(1f))
            NotificationBellButton(
                unreadCount = unreadNotificationsCount,
                onClick = onNotificationsClick,
            )

        }
        Text(
            text = greeting,
            style = PaquitoTypography.DisplayLarge,
            color = PaquitoColors.TextHomeStrong,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = dateLabel,
            style = PaquitoTypography.HeadlineSmall,
            color = PaquitoColors.TextHomeMuted,
        )

    }
}

/* -----------------------------------------------------------
 * 2. Semana: calendario compacto con los 7 dias (Lu..Do)
 * ----------------------------------------------------------- */

@Composable
private fun HomeWeekSection(
    title: String,
    days: List<WeekDayData>,
    selectedDayNumber: String?,
    onDaySelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .paquitoSurface(PaquitoShapes.card)
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            days.forEach { day ->
                WeekDayCell(
                    day = day,
                    selected = day.dayNumber == selectedDayNumber,
                    onClick = { onDaySelected(day.dayNumber) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Abrevia el nombre del dia a 2 letras para que quepan las 7 celdas en una
 * sola fila sin ambiguedad (Martes/Miercoles ambos empiezan con "M", por
 * eso 1 sola letra no alcanza): Lu, Ma, Mi, Ju, Vi, Sa, Do.
 */
private fun shortDayLabel(dayOfWeek: String): String = dayOfWeek.take(2)

/** Un dia se destaca en azul cuando filtra las tareas; el punto marca hoy o una falta. */
@Composable
private fun WeekDayCell(
    day: WeekDayData,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 72.dp)
            .clip(shape)
            .background(if (selected) PaquitoColors.StateInfoStrong else PaquitoColors.Background)
            .clickable(onClick = onClick)
            .padding(horizontal = 2.dp, vertical = 9.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = shortDayLabel(day.dayOfWeek),
            fontSize = 12.sp,
            lineHeight = 15.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = if (selected) PaquitoColors.TextOnWhite else PaquitoColors.TextOnCardMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = day.dayNumber,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = if (selected) PaquitoColors.TextOnWhite else PaquitoColors.TextOnCardStrong,
            maxLines = 1,
        )
        Box(
            modifier = Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(
                    when {
                        day.isCritical -> PaquitoColors.StateDanger
                        day.isToday && selected -> PaquitoColors.TextOnWhite
                        day.isToday -> PaquitoColors.StateInfoStrong
                        else -> Color.Transparent
                    },
                ),
        )
    }
}

/* -----------------------------------------------------------
 * 3. Tareas pendientes: tarjetas legibles dentro del scroll de Inicio
 * ----------------------------------------------------------- */

/**
 * La lista muestra todas las entregas sin calificar por defecto. Al tocar
 * un dia del calendario se filtra a esa fecha; el control "Ver todas"
 * restablece la lista completa.
 */
@Composable
private fun HomeTasksSection(
    title: String,
    dateLabel: String?,
    items: List<TaskEntry>,
    onShowAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = title,
                fontSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeStrong,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (dateLabel == null) "${items.size} en total" else "Ver todas",
                style = PaquitoTypography.Caption,
                color = PaquitoColors.TextLink,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(PaquitoColors.OverlayInfoSubtle)
                    .then(if (dateLabel != null) Modifier.clickable(onClick = onShowAll) else Modifier)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
            )
        }
        Text(
            text = if (dateLabel == null) "Todas las entregas próximas" else "Entregas del $dateLabel",
            style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.TextOnCardMuted,
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (items.isEmpty()) {
                Text(
                    text = "No hay entregas pendientes para esta fecha",
                    style = PaquitoTypography.BodyMedium,
                    color = PaquitoColors.TextOnCardMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(PaquitoShapes.card)
                        .background(PaquitoColors.SurfaceGlassSoft)
                        .padding(20.dp),
                )
            } else {
                items.forEach { item -> TaskInfoRow(item = item) }
            }
        }
    }
}

/** Una entrega por tarjeta, con el curso, el título y la fecha claramente separados. */
@Composable
private fun TaskInfoRow(item: TaskEntry) {
    val accent = when (item.urgency) {
        TaskUrgency.Urgent -> PaquitoColors.StateDanger
        TaskUrgency.Normal -> PaquitoColors.StateInfoStrong
        TaskUrgency.Future -> PaquitoColors.StateNeutral
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .paquitoSurface()
            .padding(horizontal = 14.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(PaquitoColors.OverlayInfoSubtle),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = item.iconRes),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = item.label,
                style = PaquitoTypography.Caption,
                color = PaquitoColors.TextHomeTaskLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.title,
                style = PaquitoTypography.TaskTitle.copy(fontSize = 15.sp, lineHeight = 19.sp),
                color = PaquitoColors.TextOnCardStrong,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Vence",
                style = PaquitoTypography.Caption,
                color = PaquitoColors.TextOnCardMuted,
            )
            Text(
                text = item.timestamp,
                style = PaquitoTypography.ButtonLabel,
                color = accent,
                maxLines = 1,
            )
        }
    }
}

/* -----------------------------------------------------------
 * Modelos de datos
 * ----------------------------------------------------------- */

/** Datos de un dia del calendario semanal. */
data class WeekDayData(
    val dayOfWeek: String, // "Lunes", "Martes", "Miercoles", ...
    val dayNumber: String, // "03", "04", ... - numero mostrado en el calendario semanal
    val dateLabel: String, // "03/08/26" - se muestra en el header de "Tareas Pendientes" al seleccionar este dia
    val isToday: Boolean,  // identifica el dia actual en el calendario
    val isCritical: Boolean,
    val dateKey: String = "", // "yyyy-MM-dd", evita filtrar tareas por numero de dia solamente
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
    val dateKey: String = "", // "yyyy-MM-dd", permite distinguir fechas de meses distintos
)

/** Elemento breve que alimenta los widgets de prioridad en Inicio. */
data class HomeFocusItem(
    val title: String,
    val detail: String,
)

/**
 * Ranking de un ciclo YA FINALIZADO (1° al 6°, nunca el ciclo en curso -
 * ver comentario de `PainSnapshotRow`). `ranking` va de 0 a 20 con
 * decimales, igual que las notas.
 */
data class CycleRanking(
    val cycle: Int,
    val ranking: Double,
)

/** Snapshot de la pantalla Home, armado por `AcademicHomeViewModel` desde el JSON academico local. */
data class HomeScreenData(
    val greeting: String,
    val dateLabel: String,
    val weekTitle: String,
    val days: List<WeekDayData>,
    val tasksHeader: String,
    val tasks: List<TaskEntry>,
    val pendingCount: Int,
    // Ranking academico general (acumulado, todos los ciclos antes del
    // actual) y ranking por ciclo (seleccionable, ver `PainSnapshotRow`) -
    // dato de DEMO, el JSON local no tiene este concepto.
    val overallRanking: Double,
    val careerCode: String,
    val currentCycle: Int,
    val cycleRankings: List<CycleRanking>,
    val importantNotices: List<HomeFocusItem> = emptyList(),
    val importantNoticeCount: Int = 0,
    // `pendingCount` de arriba es el badge del FAB de Paquito (tareas
    // pendientes) - este es un contador DISTINTO para la campana del header
    // (avisos generales: vencimientos, notas nuevas, sync de datos). Se
    // muestran juntos en pantalla, pero no deberian ser el mismo numero:
    // implicaria que son el mismo concepto y no lo son.
    val unreadNotificationsCount: Int = 0,
    // LEGADO: las cards de promedio/faltas/entregas ya no se dibujan en Home
    // (las reemplazo el ranking), pero `MockAcademicContext` (contexto del
    // chat, que sigue con datos demo) todavia los lee desde `default()`.
    // Borrar junto con ese uso cuando el chat lea el JSON local.
    val trackValue: String = "—",
    val absencesUsed: Int = 0,
    val absencesLimit: Int = 0,
    val dueThisWeek: Int = 0,
) {
    companion object {
        fun default(): HomeScreenData = HomeScreenData(
            greeting = "¡Bienvenido, estudiante!",
            dateLabel = "Lunes, 2 de noviembre de 2026",
            weekTitle = "Semana 10",
            days = listOf(
                WeekDayData("Lunes", "02", "02/11/26", isToday = true,  isCritical = false, dateKey = "2026-11-02"),
                WeekDayData("Martes", "03", "03/11/26", isToday = false, isCritical = false, dateKey = "2026-11-03"),
                WeekDayData("Miercoles", "04", "04/11/26", isToday = false, isCritical = false, dateKey = "2026-11-04"),
                WeekDayData("Jueves", "05", "05/11/26", isToday = false, isCritical = false, dateKey = "2026-11-05"),
                WeekDayData("Viernes", "06", "06/11/26", isToday = false, isCritical = false, dateKey = "2026-11-06"),
                WeekDayData("Sabado", "07", "07/11/26", isToday = false, isCritical = true, dateKey = "2026-11-07"),
                WeekDayData("Domingo", "08", "08/11/26", isToday = false, isCritical = false, dateKey = "2026-11-08"),
            ),
            tasksHeader = "Tareas Pendientes",
            // Domingo (08) queda a proposito sin ninguna tarea, para probar
            // el estado vacio "No hay tareas pendientes".
            tasks = listOf(
                TaskEntry(
                    label = "Calculo II · lab",
                    title = "Laboratorio 4",
                    timestamp = "12 h",
                    urgency = TaskUrgency.Urgent,
                    iconRes = R.drawable.ic_paquito_lab_profile,
                    dateKey = "2026-11-02",
                ),
                TaskEntry(
                    label = "Algoritmos · foro",
                    title = "Foro 2",
                    timestamp = "2 d",
                    urgency = TaskUrgency.Urgent,
                    iconRes = R.drawable.ic_paquito_docs_default,
                    dateKey = "2026-11-04",
                ),
                TaskEntry(
                    label = "Fisica I · practica",
                    title = "Practica 3",
                    timestamp = "3 d",
                    urgency = TaskUrgency.Normal,
                    iconRes = R.drawable.ic_paquito_docs_default,
                    dateKey = "2026-11-05",
                ),
                TaskEntry(
                    label = "Calculo II · practica",
                    title = "Practica 3",
                    timestamp = "7 d",
                    urgency = TaskUrgency.Future,
                    iconRes = R.drawable.ic_paquito_docs_default,
                    dateKey = "2026-11-07",
                ),
            ),
            pendingCount = 4,
            overallRanking = 15.0,
            careerCode = "C-24",
            currentCycle = 6,
            cycleRankings = listOf(
                CycleRanking(cycle = 1, ranking = 14.2),
                CycleRanking(cycle = 2, ranking = 15.6),
                CycleRanking(cycle = 3, ranking = 13.8),
                CycleRanking(cycle = 4, ranking = 16.4),
                CycleRanking(cycle = 5, ranking = 15.1),
            ),
            importantNotices = listOf(
                HomeFocusItem("Asistencia en riesgo", "Revisa tus faltas en Redes I"),
                HomeFocusItem("Nota por mejorar", "Laboratorio 1 · Redes y Comunicaciones · 9/20"),
            ),
            importantNoticeCount = 2,
            unreadNotificationsCount = 2,
            trackValue = "14.8",
            absencesUsed = 2,
            absencesLimit = 5,
            dueThisWeek = 3,
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
