package pe.tecsup.paquitobot.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import pe.tecsup.paquitobot.ui.components.AppTabScaffold
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.NotificationBellButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Pantalla Home de PaquitoBot, fiel al frame Figma `351:644` del archivo
 * "Paquito (copia)" (canvas Main `109:97`).
 *
 * Estructura (de arriba hacia abajo):
 *   1. Header / saludo     ("¡Bienvenido, {nombre}!" + fecha completa) — alineado a izquierda
 *   2. Snapshot de los 3 dolores (promedio / faltas / entregas)
 *   3. Calendario semanal  ("Semana 10" + card oscura con 7 dias en 1 sola fila)
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
    onTrackClick: () -> Unit = { onTabSelected(NavTab.Cursos) },
    onAbsencesClick: () -> Unit = { onTabSelected(NavTab.Horarios) },
    onNotificationsClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Iteracion 2026-08-13 (filtro por dia): que dia esta seleccionado en el
    // calendario semanal ahora vive como estado de UI (antes `WeekDayData.selected`
    // era estatico, ni siquiera clickeable). Arranca en el dia marcado `isToday`.
    var selectedDayNumber by remember(data) {
        mutableStateOf(data.days.firstOrNull { it.isToday }?.dayNumber ?: data.days.firstOrNull()?.dayNumber.orEmpty())
    }
    val selectedDay = data.days.firstOrNull { it.dayNumber == selectedDayNumber }
    val tasksForSelectedDay = data.tasks.filter { it.dayNumber == selectedDayNumber }

    AppTabScaffold(
        currentTab = currentTab,
        onTabSelected = onTabSelected,
        onPaquitoClick = onPaquitoClick,
        notificationCount = data.pendingCount,
        modifier = modifier,
    ) {
        HomeMessage(
            greeting = data.greeting,
            dateLabel = data.dateLabel,
            unreadNotificationsCount = data.unreadNotificationsCount,
            onNotificationsClick = onNotificationsClick,
        )
        PainSnapshotRow(
            trackValue = data.trackValue,
            absencesUsed = data.absencesUsed,
            absencesLimit = data.absencesLimit,
            dueThisWeek = data.dueThisWeek,
            onTrackClick = onTrackClick,
            onAbsencesClick = onAbsencesClick,
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
        )
    }
}

@Composable
private fun PainSnapshotRow(
    trackValue: String,
    absencesUsed: Int,
    absencesLimit: Int,
    dueThisWeek: Int,
    onTrackClick: () -> Unit,
    onAbsencesClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        SnapshotChip(
            label = "Como vas",
            value = trackValue,
            hint = "de 20",
            onClick = onTrackClick,
            modifier = Modifier.weight(1f),
        )
        SnapshotChip(
            label = "Faltas",
            value = "$absencesUsed/$absencesLimit",
            hint = "limite",
            onClick = onAbsencesClick,
            modifier = Modifier.weight(1f),
            valueColor = if (absencesUsed >= absencesLimit - 1) {
                PaquitoColors.StateDanger
            } else {
                PaquitoColors.StateWarning
            },
        )
        SnapshotChip(
            label = "Entregas",
            value = "$dueThisWeek",
            hint = "esta semana",
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
            .clip(RoundedCornerShape(20.dp))
            .background(PaquitoColors.SurfaceElevated)
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
            )
            .padding(horizontal = 10.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeTaskLabel,
            maxLines = 1,
        )
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = valueColor,
            maxLines = 1,
        )
        Text(
            text = hint,
            fontSize = 10.sp,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextOnCardMuted,
            maxLines = 1,
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
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalAlignment = Alignment.Start, // <-- antes Center; ahora Start segun Figma
            modifier = Modifier.weight(1f),
        ) {
            Text(
                text = greeting,
                fontSize = 26.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeStrong,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
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
        NotificationBellButton(
            unreadCount = unreadNotificationsCount,
            onClick = onNotificationsClick,
        )
    }
}

/* -----------------------------------------------------------
 * 2. Semana: titulo + card oscura con 7 dias en 1 sola fila (Lu..Do)
 * ----------------------------------------------------------- */

@Composable
private fun HomeWeekSection(
    title: String,
    days: List<WeekDayData>,
    selectedDayNumber: String,
    onDaySelected: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(
            text = title,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
        )

        // Card oscura `SurfaceHomeWeekBg` con los 7 dias en UNA sola fila
        // (tira de calendario semanal). Evita el dia "huerfano" que quedaba
        // solo en una tercera fila con el grid 3x3 anterior.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(35.dp))
                .background(PaquitoColors.SurfaceHomeWeekBg)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
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

/**
 * Celda de un dia en el calendario semanal del Home A.
 *
 * Variantes visuales (sacadas de Figma 351:644):
 *   - dia seleccionado:  bg rgba(211,211,211,0.28), label blanco translucido.
 *   - dia normal:        bg rgba(255,255,255,0.9), label gris translucido.
 *   - numero: 24sp (compactado para caber en el cell); color rojo translucido
 *     si es critico, cyan translucido en otro caso.
 *
 * Iteracion 2026-08-06: los 7 dias (incluyendo Domingo) van en UNA sola fila
 * en vez de un grid 3x3 (el grid dejaba el Domingo huerfano, solo, en una
 * tercera fila descentrada). Con 7 celdas en fila no entra el nombre
 * completo del dia, por eso se abrevia a 2 letras via [shortDayLabel].
 *
 * Iteracion 2026-08-13 (filtro por dia): la celda pasa de ser puramente
 * visual a interactiva - [onClick] filtra "Tareas Pendientes" por el dia
 * tocado (antes ningun dia era clickeable).
 */
@Composable
private fun WeekDayCell(
    day: WeekDayData,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val cellBg = if (selected) {
        androidx.compose.ui.graphics.Color(0x47D3D3D3) // rgba(211,211,211,0.28)
    } else {
        androidx.compose.ui.graphics.Color(0xE6FFFFFF) // rgba(255,255,255,0.9)
    }
    val labelColor = if (selected) {
        PaquitoColors.TextHomeDayActiveLabel
    } else {
        PaquitoColors.TextHomeDayLabel
    }
    val numberColor = if (day.isCritical) {
        PaquitoColors.TextHomeDayNumberCritical
    } else {
        PaquitoColors.TextHomeDayNumber
    }
    // Cell compacto: 64dp de altura minima, ancho flexible (weight 1f) para
    // que las 7 celdas de la fila unica se repartan el ancho disponible.
    // El contenido del cell esta centrado horizontalmente para evitar que
    // los labels cortos ("Lunes", "Jueves") queden pegados a la izquierda
    // con un hueco blanco visible a la derecha cuando el cell se estira
    // con weight(1f). El Figma real muestra el "03" centrado dentro del
    // cell, no alineado a Start.
    Column(
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(cellBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = shortDayLabel(day.dayOfWeek),
            fontSize = 11.sp,
            lineHeight = 13.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = labelColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = day.dayNumber,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = numberColor,
            maxLines = 1,
        )
    }
}

/* -----------------------------------------------------------
 * 3. Tareas pendientes: titulo + icono foro + card translucida con lista
 * ----------------------------------------------------------- */

/**
 * Iteracion 2026-08-13 (filtro por dia): [title] ahora se completa con la
 * fecha numerica del dia seleccionado ("Tareas Pendientes — 03/08/26"), y
 * la lista se filtra a las tareas de ESE dia. Si no hay ninguna (caso
 * esperable un Domingo), se muestra un estado vacio en vez de un card sin
 * contenido.
 */
@Composable
private fun HomeTasksSection(title: String, dateLabel: String?, items: List<TaskEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Header de la seccion: solo el titulo.
        // Iteracion 2026-08-06: el icono de foro (2 burbujas de chat) que
        // habia al costado del titulo se elimina - el frame Figma actual
        // (re-extraido de "Paquito-v2") ya NO lo tiene, era de una
        // iteracion vieja del diseno.
        Text(
            text = if (dateLabel != null) "$title — $dateLabel" else title,
            fontSize = 20.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.SemiBold,
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextHomeStrong,
            modifier = Modifier.fillMaxWidth(),
        )

        // Card wrapper translucida con la lista adentro.
        // Iteracion 2026-08-06 01:51: se RESTAURA el wrapper del card que
        // habia sido eliminado por error en la iteracion anterior. El card
        // debe existir (es parte del diseno Figma) y debe tener un BORDE
        // sutil blanco + fondo blanco translucido. El feedback del usuario
        // decia "agregar el contorno del card de tareas", NO eliminarlo.
        //
        //   fondo: rgba(245,245,245,0.2)  = #33F5F5F5
        //   borde: rgba(255,255,255,0.4)  = #66FFFFFF (sutil, fino, 1dp)
        //   radio: 28dp (mismo que el card de la semana)
        //   padding interno: 6dp
        val taskScroll = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 80.dp)
                .heightIn(max = 280.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(PaquitoColors.SurfaceHomeTaskListBg)
                .border(
                    width = 1.dp,
                    color = androidx.compose.ui.graphics.Color(0x66FFFFFF),
                    shape = RoundedCornerShape(35.dp),
                )
                .verticalScroll(taskScroll)
                .padding(6.dp),
        ) {
            if (items.isEmpty()) {
                Text(
                    text = "No hay tareas pendientes",
                    fontSize = 14.sp,
                    fontFamily = PaquitoFont.DMSans,
                    color = PaquitoColors.TextOnCardMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 20.dp),
                )
            } else {
                items.forEachIndexed { index, item ->
                    if (index > 0) {
                        TaskDivider()
                    }
                    TaskInfoRow(item = item)
                }
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
 *   [icono 18x18] [col (label "Curso" + titulo "Nombre Tarea")] [timestamp 24sp]
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
 *   - Compactado (padding 10/6dp, fuentes 11/14sp, timestamp 24sp) para que el
 *     item no se sienta ancho en la nueva card con padding 6dp.
 */
@Composable
private fun TaskInfoRow(item: TaskEntry) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = item.iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            contentScale = ContentScale.Fit,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(1.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            Text(
                text = item.label,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeTaskLabel,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.title,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextOnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.timestamp,
            fontSize = 24.sp,
            lineHeight = 28.sp,
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
    val dayNumber: String, // "03", "04", ... - clave para filtrar tareas (ver [TaskEntry.dayNumber])
    val dateLabel: String, // "03/08/26" - se muestra en el header de "Tareas Pendientes" al seleccionar este dia
    val isToday: Boolean,  // dia con el que arranca seleccionado el calendario (antes se llamaba `selected`, ahora eso es estado de UI)
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
    val dayNumber: String, // coincide con [WeekDayData.dayNumber] del dia al que pertenece esta tarea
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
    val trackValue: String,
    val absencesUsed: Int,
    val absencesLimit: Int,
    val dueThisWeek: Int,
    // `pendingCount` de arriba es el badge del FAB de Paquito (tareas
    // pendientes) - este es un contador DISTINTO para la campana del header
    // (avisos generales: vencimientos, notas nuevas, sync de Canvas). Se
    // muestran juntos en pantalla, pero no deberian ser el mismo numero:
    // implicaria que son el mismo concepto y no lo son.
    val unreadNotificationsCount: Int = 0,
) {
    companion object {
        fun default(): HomeScreenData = HomeScreenData(
            greeting = "¡Bienvenido, estudiante!",
            dateLabel = "Lunes, 3 de agosto de 2026",
            weekTitle = "Semana 10",
            days = listOf(
                WeekDayData("Lunes",     "03", "03/08/26", isToday = true,  isCritical = false),
                WeekDayData("Martes",    "04", "04/08/26", isToday = false, isCritical = false),
                WeekDayData("Miercoles", "05", "05/08/26", isToday = false, isCritical = false),
                WeekDayData("Jueves",    "06", "06/08/26", isToday = false, isCritical = false),
                WeekDayData("Viernes",   "07", "07/08/26", isToday = false, isCritical = false),
                WeekDayData("Sabado",    "08", "08/08/26", isToday = false, isCritical = true),
                WeekDayData("Domingo",   "09", "09/08/26", isToday = false, isCritical = false),
            ),
            tasksHeader = "Tareas Pendientes",
            // Domingo (09) queda a proposito sin ninguna tarea, para probar
            // el estado vacio "No hay tareas pendientes".
            tasks = listOf(
                TaskEntry(
                    label = "Calculo II · lab",
                    title = "Laboratorio 4",
                    timestamp = "12 h",
                    urgency = TaskUrgency.Urgent,
                    dayNumber = "03",
                    iconRes = R.drawable.ic_paquito_lab_profile,
                ),
                TaskEntry(
                    label = "Algoritmos · foro",
                    title = "Foro 2",
                    timestamp = "2 d",
                    urgency = TaskUrgency.Urgent,
                    dayNumber = "05",
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
                TaskEntry(
                    label = "Fisica I · practica",
                    title = "Practica 3",
                    timestamp = "3 d",
                    urgency = TaskUrgency.Normal,
                    dayNumber = "06",
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
                TaskEntry(
                    label = "Calculo II · practica",
                    title = "Practica 3",
                    timestamp = "7 d",
                    urgency = TaskUrgency.Future,
                    dayNumber = "08",
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
            ),
            pendingCount = 3,
            trackValue = "14.8",
            absencesUsed = 2,
            absencesLimit = 5,
            dueThisWeek = 3,
            unreadNotificationsCount = 2,
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