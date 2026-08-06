package pe.tecsup.paquitobot.ui.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
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
 * "Paquito (copia)" (canvas Main `109:97`).
 *
 * Estructura (de arriba hacia abajo):
 *   1. Header / saludo     ("¡Bienvenido, {nombre}!" + fecha completa) — alineado a izquierda
 *   2. Calendario semanal  ("Semana 10" + card oscura con 7 dias en 1 sola fila)
 *   3. Tareas pendientes   ("Tareas Pendientes" + icono foro + lista con timestamps)
 *   4. Navbar inferior     (3 tabs + boton Paquito + badge "3")
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
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.SurfaceHomeCanvas),
    ) {
        // Cuerpo principal: padding 24dp lateral (proporcional), top 50dp.
        // 24dp en vez de 20dp para que el contenido respire igual a izquierda
        // y derecha en pantallas 360-411dp.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 24.dp, top = 64.dp, end = 24.dp, bottom = 110.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            // 1. Saludo + fecha.
            HomeMessage(
                greeting = data.greeting,
                dateLabel = data.dateLabel,
            )

            // 2 + 3. Calendario semanal + tareas pendientes.
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

        // 4. Navbar inferior (centrada horizontal, separada del borde).
        // Iteracion 2026-08-06 01:37: la Navbar quedaba pegada al borde
        // inferior de la pantalla. Se agrega padding(bottom = 24dp) al Box
        // contenedor para que respire del borde inferior. El padding lateral
        // lo maneja el propio Navbar mediante `horizontalPadding`.
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

/* -----------------------------------------------------------
 * 1. Header: saludo + fecha completa — alineado a la IZQUIERDA
 * ----------------------------------------------------------- */

@Composable
private fun HomeMessage(greeting: String, dateLabel: String) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start, // <-- antes Center; ahora Start segun Figma
        modifier = Modifier.fillMaxWidth(),
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
}

/* -----------------------------------------------------------
 * 2. Semana: titulo + card oscura con 7 dias en 1 sola fila (Lu..Do)
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
                WeekDayCell(day = day, modifier = Modifier.weight(1f))
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
 *   - dia actual (selected=true):  bg rgba(211,211,211,0.28), label blanco translucido.
 *   - dia normal (selected=false): bg rgba(255,255,255,0.9), label gris translucido.
 *   - numero: 24sp (compactado para caber en el cell); color rojo translucido
 *     si es critico, cyan translucido en otro caso.
 *
 * Iteracion 2026-08-06: los 7 dias (incluyendo Domingo) van en UNA sola fila
 * en vez de un grid 3x3 (el grid dejaba el Domingo huerfano, solo, en una
 * tercera fila descentrada). Con 7 celdas en fila no entra el nombre
 * completo del dia, por eso se abrevia a 2 letras via [shortDayLabel].
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

@Composable
private fun HomeTasksSection(title: String, items: List<TaskEntry>) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        // Header de la seccion: solo el titulo.
        // Iteracion 2026-08-06: el icono de foro (2 burbujas de chat) que
        // habia al costado del titulo se elimina - el frame Figma actual
        // (re-extraido de "Paquito-v2") ya NO lo tiene, era de una
        // iteracion vieja del diseno.
        Text(
            text = title,
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
            // 7 dias: L M M | J V S | D (grid 3x3, 3 filas).
            // Domingo va solo en la ultima fila (alineado a la izquierda).
            days = listOf(
                WeekDayData("Lunes",     "03", selected = true,  isCritical = false),
                WeekDayData("Martes",    "04", selected = false, isCritical = false),
                WeekDayData("Miercoles", "05", selected = false, isCritical = false),
                WeekDayData("Jueves",    "06", selected = false, isCritical = false),
                WeekDayData("Viernes",   "07", selected = false, isCritical = false),
                WeekDayData("Sabado",    "08", selected = false, isCritical = true),
                WeekDayData("Domingo",   "09", selected = false, isCritical = false),
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