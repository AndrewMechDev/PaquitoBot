package pe.tecsup.paquitobot.ui.notifications

import androidx.annotation.DrawableRes
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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Bandeja de notificaciones, abierta desde la campana del Home
 * ([pe.tecsup.paquitobot.ui.components.NotificationBellButton]).
 *
 * NO confundir con `NotificationCard`/`NotificationType`
 * (`ui/components/NotificationCard.kt`) - ese es el selector de
 * PREFERENCIAS de aviso del onboarding, un concepto distinto a esta
 * bandeja. Nombrado distinto a proposito (skill `senior-review-paquitobot`).
 *
 * Sin frame de Figma - reusa el patron de fila de `TaskInfoRow` (Home) y el
 * toggle Compacto/Lista del onboarding (`OnboardingNotificationsScreen`)
 * para consistencia visual, en vez de inventar layouts nuevos.
 *
 * Iteracion 2026-08-13 (gestion de notificaciones):
 * - Toggle de layout (cuadros/lista), mismo patron que el onboarding.
 * - Swipe-to-delete por notificacion (fondo rojo + "-"), con Snackbar
 *   "Deshacer" en vez de un popup de confirmacion por swipe - pedir
 *   confirmacion en cada swipe es friccion innecesaria (ningun cliente de
 *   mail/mensajeria lo hace); en cambio, "eliminar todas" SI pide
 *   confirmacion via AlertDialog porque es una accion masiva irreversible.
 * - Tocar una notificacion abre un popup con el detalle completo
 *   estructurado (titulo + cuerpo + hora), sin recortar texto largo.
 * - Estado local con `mutableStateListOf` (no ViewModel): no hay backend
 *   de notificaciones todavia, sigue la convencion de `HomeScreenData`
 *   (skill `architecture-paquitobot`).
 */
@Composable
fun NotificationsInboxScreen(
    data: NotificationsInboxData = NotificationsInboxData.default(),
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val items = remember(data) { mutableStateListOf(*data.items.toTypedArray()) }
    var variant by remember { mutableStateOf(NotificationCardVariant.Full) }
    var selectedItem by remember { mutableStateOf<NotificationInboxItem?>(null) }
    var confirmDeleteAll by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun deleteItem(item: NotificationInboxItem) {
        val index = items.indexOf(item)
        if (index == -1) return
        items.removeAt(index)
        coroutineScope.launch {
            val result = snackbarHostState.showSnackbar(
                message = "Notificación eliminada",
                actionLabel = "Deshacer",
                duration = SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                items.add(index.coerceIn(0, items.size), item)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(PaquitoColors.Background)
                .systemBarsPadding()
                .padding(horizontal = PaquitoSpacing.lg)
                .padding(top = 8.dp, bottom = PaquitoSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_paquito_arrow_back),
                    contentDescription = "Volver",
                    modifier = Modifier
                        .size(21.dp)
                        .clickable(onClick = onBackClick),
                    contentScale = ContentScale.Fit,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    LayoutToggleButton(
                        variant = variant,
                        onToggle = {
                            variant = if (variant == NotificationCardVariant.Full) {
                                NotificationCardVariant.Compact
                            } else {
                                NotificationCardVariant.Full
                            }
                        },
                    )
                    if (items.isNotEmpty()) {
                        DeleteAllButton(onClick = { confirmDeleteAll = true })
                    }
                }
            }

            Text(
                text = "Notificaciones",
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextHomeStrong,
            )

            if (items.isEmpty()) {
                EmptyState()
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    when (variant) {
                        NotificationCardVariant.Full -> items.forEach { item ->
                            SwipeableNotification(
                                onDelete = { deleteItem(item) },
                            ) {
                                NotificationInboxRow(item, onClick = { selectedItem = item })
                            }
                        }
                        NotificationCardVariant.Compact -> items.chunked(2).forEach { row ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                            ) {
                                row.forEach { item ->
                                    SwipeableNotification(
                                        modifier = Modifier.weight(1f),
                                        onDelete = { deleteItem(item) },
                                    ) {
                                        NotificationInboxCard(item, onClick = { selectedItem = item })
                                    }
                                }
                                if (row.size == 1) {
                                    Box(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 24.dp),
        )
    }

    selectedItem?.let { item ->
        NotificationDetailDialog(item = item, onDismiss = { selectedItem = null })
    }

    if (confirmDeleteAll) {
        AlertDialog(
            onDismissRequest = { confirmDeleteAll = false },
            confirmButton = {
                TextButton(onClick = { items.clear(); confirmDeleteAll = false }) {
                    Text("Eliminar todas")
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDeleteAll = false }) { Text("Cancelar") }
            },
            title = { Text("¿Eliminar todas las notificaciones?") },
            text = { Text("Esta acción no se puede deshacer.") },
        )
    }
}

/** Icono chico que alterna entre grilla (Compact) y lista (Full). Dibujado con primitivas, sin libreria de iconos. */
@Composable
private fun LayoutToggleButton(variant: NotificationCardVariant, onToggle: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PaquitoColors.SurfaceElevated)
            .clickable(
                onClickLabel = if (variant == NotificationCardVariant.Full) "Ver en cuadros" else "Ver en lista",
                onClick = onToggle,
            ),
        contentAlignment = Alignment.Center,
    ) {
        when (variant) {
            // Mostrando Lista -> el icono ofrece pasar a Cuadros (grilla 2x2).
            NotificationCardVariant.Full -> Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                repeat(2) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        repeat(2) {
                            Box(
                                Modifier
                                    .size(6.dp)
                                    .clip(RoundedCornerShape(1.5.dp))
                                    .background(PaquitoColors.TextOnSurface),
                            )
                        }
                    }
                }
            }
            // Mostrando Cuadros -> el icono ofrece pasar a Lista (3 lineas).
            NotificationCardVariant.Compact -> Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                repeat(3) {
                    Box(
                        Modifier
                            .width(16.dp)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(PaquitoColors.TextOnSurface),
                    )
                }
            }
        }
    }
}

/**
 * Boton "eliminar todas" - icono de tacho dibujado con primitivas (tapa +
 * cuerpo con borde), mismo tamaño/estilo circular que [LayoutToggleButton]
 * para que ambos iconos del header se vean como un par consistente.
 * Reemplaza el texto "Eliminar todas" (feedback del usuario: usar icono).
 */
@Composable
private fun DeleteAllButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(PaquitoColors.StateDanger.copy(alpha = 0.12f))
            .clickable(onClickLabel = "Eliminar todas las notificaciones", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            // Tapa del tacho.
            Box(
                Modifier
                    .width(14.dp)
                    .height(2.dp)
                    .clip(RoundedCornerShape(1.dp))
                    .background(PaquitoColors.StateDanger),
            )
            // Cuerpo del tacho.
            Box(
                modifier = Modifier
                    .width(11.dp)
                    .height(10.dp)
                    .clip(RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp, topStart = 1.dp, topEnd = 1.dp))
                    .border(
                        width = 1.5.dp,
                        color = PaquitoColors.StateDanger,
                        shape = RoundedCornerShape(bottomStart = 3.dp, bottomEnd = 3.dp, topStart = 1.dp, topEnd = 1.dp),
                    ),
            )
        }
    }
}

/** Variante visual de la bandeja: [Full] = lista de filas, [Compact] = grilla 2 columnas. */
enum class NotificationCardVariant { Full, Compact }

@Composable
private fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "No tenés notificaciones por ahora",
            style = PaquitoTypography.BodyLarge,
            color = PaquitoColors.TextHomeMuted,
        )
    }
}

/**
 * Envuelve [content] en `SwipeToDismissBox`: swipe de derecha a izquierda
 * revela un fondo rojo con "-" y dispara [onDelete] al completarse. Sin
 * confirmacion aca (ver nota en [NotificationsInboxScreen]) - el
 * `Snackbar` "Deshacer" que arma [onDelete] es la red de seguridad.
 */
@Composable
private fun SwipeableNotification(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = { SwipeDeleteBackground() },
    ) {
        content()
    }
}

@Composable
private fun SwipeDeleteBackground() {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.StateDanger)
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f)),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                Modifier
                    .width(14.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(PaquitoColors.TextOnWhite),
            )
        }
    }
}

/** Fila de una notificacion (variante Lista), mismo layout que `TaskInfoRow` del Home. */
@Composable
private fun NotificationInboxRow(item: NotificationInboxItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NotificationIcon(item.iconRes)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = item.title,
                style = PaquitoTypography.TaskTitle,
                color = PaquitoColors.TextOnCardStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = item.body,
                style = PaquitoTypography.TaskSubtitle,
                color = PaquitoColors.TextOnCardMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = item.timestamp,
            style = PaquitoTypography.Timestamp,
            color = if (item.severity == NotificationSeverity.Urgent) {
                PaquitoColors.StateDanger
            } else {
                PaquitoColors.TextTimestamp
            },
            softWrap = false,
        )
    }
}

/** Card de una notificacion (variante Cuadros), mismo patron que `NotificationCard` (onboarding). */
@Composable
private fun NotificationInboxCard(item: NotificationInboxItem, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.SurfaceElevated)
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Bottom),
    ) {
        NotificationIcon(item.iconRes)
        Text(
            text = item.title,
            style = PaquitoTypography.TaskTitle,
            color = PaquitoColors.TextOnCardStrong,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = item.timestamp,
            style = PaquitoTypography.Timestamp,
            color = if (item.severity == NotificationSeverity.Urgent) {
                PaquitoColors.StateDanger
            } else {
                PaquitoColors.TextTimestamp
            },
        )
    }
}

@Composable
private fun NotificationIcon(@DrawableRes iconRes: Int) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(PaquitoColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            contentScale = ContentScale.Fit,
        )
    }
}

/**
 * Popup con el detalle completo y estructurado de una notificacion -
 * titulo, cuerpo sin recortar y hora, cada uno en su propia seccion.
 */
@Composable
private fun NotificationDetailDialog(item: NotificationInboxItem, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Cerrar") } },
        title = { Text(item.title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = item.body,
                    style = PaquitoTypography.BodyLarge,
                    color = PaquitoColors.TextOnCardStrong,
                )
                Text(
                    text = "Hace ${item.timestamp}",
                    style = PaquitoTypography.BodySmall,
                    color = PaquitoColors.TextHomeMuted,
                )
            }
        },
    )
}

/** Urgencia visual de una notificacion (afecta solo el color del timestamp). */
enum class NotificationSeverity { Info, Urgent }

/** Item individual de la bandeja de notificaciones. */
data class NotificationInboxItem(
    val title: String,
    val body: String,
    val timestamp: String,
    val severity: NotificationSeverity,
    @DrawableRes val iconRes: Int,
)

/** Snapshot de la bandeja de notificaciones. Vendra del Repository cuando exista backend de avisos. */
data class NotificationsInboxData(
    val items: List<NotificationInboxItem>,
) {
    companion object {
        fun default(): NotificationsInboxData = NotificationsInboxData(
            items = listOf(
                NotificationInboxItem(
                    title = "Laboratorio 4 vence pronto",
                    body = "Cálculo II · 15% de la nota final. Entregá el informe completo con las 5 preguntas resueltas antes de las 23:59 para no perder puntaje.",
                    timestamp = "12 h",
                    severity = NotificationSeverity.Urgent,
                    iconRes = R.drawable.ic_paquito_lab_profile,
                ),
                NotificationInboxItem(
                    title = "Nueva nota disponible",
                    body = "Práctica 2 calificada en Física I",
                    timestamp = "1 d",
                    severity = NotificationSeverity.Info,
                    iconRes = R.drawable.ic_paquito_docs_default,
                ),
                NotificationInboxItem(
                    title = "Canvas sincronizado",
                    body = "Tus cursos y tareas están al día",
                    timestamp = "2 d",
                    severity = NotificationSeverity.Info,
                    iconRes = R.drawable.ic_paquito_calendar_fill,
                ),
            ),
        )

        fun empty(): NotificationsInboxData = NotificationsInboxData(items = emptyList())
    }
}

@Preview(showBackground = true, name = "Lista")
@Composable
private fun NotificationsInboxScreenPreview() {
    PaquitoTheme {
        NotificationsInboxScreen()
    }
}

@Preview(showBackground = true, name = "Vacio")
@Composable
private fun NotificationsInboxScreenEmptyPreview() {
    PaquitoTheme {
        NotificationsInboxScreen(data = NotificationsInboxData.empty())
    }
}
