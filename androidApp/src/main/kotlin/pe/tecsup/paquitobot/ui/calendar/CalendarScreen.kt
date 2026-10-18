package pe.tecsup.paquitobot.ui.calendar

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pe.tecsup.paquitobot.PaquitoBotApp
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.data.calendar.GoogleCalendarAuthorization
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.procedures.GuideHeader
import pe.tecsup.paquitobot.ui.theme.*
import java.time.Instant
import java.time.format.DateTimeFormatter
import pe.tecsup.paquitobot.domain.calendar.CalendarCandidate
import pe.tecsup.paquitobot.domain.calendar.CalendarEventPlanner

@Composable
fun CalendarScreen(onBackClick: () -> Unit) {
    val context = LocalContext.current.applicationContext
    val owner = authorizedAcademicUserId()
    BackHandler(onBack = onBackClick)
    if (owner == null) {
        Column(Modifier.fillMaxSize().systemBarsPadding().padding(24.dp)) {
            GuideHeader("Calendar", "La sesión cambió. Vuelve a iniciar sesión.", onBackClick)
        }
        return
    }
    val model: CalendarViewModel = viewModel(key = "calendar-$owner", factory = remember(owner, context) {
        viewModelFactory { initializer {
            CalendarViewModel((context as PaquitoBotApp).academicRepository, GoogleCalendarAuthorization(context), owner,
                stillAuthorized = { authorizedAcademicUserId() == owner })
        } }
    })
    val state by model.uiState.collectAsStateWithLifecycle()
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        model.resolve(if (result.resultCode == Activity.RESULT_OK) result.data else null)
    }
    LaunchedEffect(state.resolution) {
        val pending = state.resolution ?: return@LaunchedEffect
        model.resolutionLaunched()
        try { launcher.launch(IntentSenderRequest.Builder(pending.intentSender).build()) }
        catch (_: Exception) { model.resolutionFailed() }
    }
    CalendarContent(state, model::authorize, model::disconnect, model::refresh, model::showPast, model::select, model::sync, onBackClick)
}

@Composable
private fun CalendarContent(state: CalendarUiState, onConnect: () -> Unit, onDisconnect: () -> Unit,
    onRefresh: () -> Unit, onShowPast: (Boolean) -> Unit, onSelect: (String) -> Unit,
    onSync: () -> Unit, onBack: () -> Unit) {
    val visible = state.candidates.filter { state.showPast || !it.past }
    Column(Modifier.fillMaxSize().background(PaquitoColors.Background).systemBarsPadding().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuideHeader("Calendar", "Tus entregas en Google Calendar", onBack)
        if (state.busy) LinearProgressIndicator(Modifier.fillMaxWidth(), color = PaquitoColors.StateInfoStrong)
        LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(14.dp), contentPadding = PaddingValues(bottom = 16.dp)) {
            item("account") {
                Column(Modifier.fillMaxWidth().paquitoSurface().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(state.email ?: "Conecta tu cuenta de Google", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
                    Text("Se crearán o actualizarán las tareas elegidas en tu calendario principal. La app no borra eventos ni modifica los de otras fuentes.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                    if (state.email == null) PaquitoPrimaryButton("Conectar Google Calendar", onConnect, enabled = !state.busy)
                    else {
                        OutlinedButton(onClick = onDisconnect, enabled = !state.busy, modifier = Modifier.fillMaxWidth()) {
                            Text("Revocar acceso de Google", style = PaquitoTypography.BodyMedium)
                        }
                        TextButton(onClick = onConnect, enabled = !state.busy) {
                            Text("Renovar autorización", style = PaquitoTypography.BodyMedium)
                        }
                    }
                    Text("Revocar el acceso conserva los eventos que ya se crearon. Google solicitará autorización nuevamente cuando vuelvas a conectar.", style = PaquitoTypography.Caption, color = PaquitoColors.TextSecondary)
                }
            }
            item("source") {
                Text("Datos académicos de demostración · horario de Perú (America/Lima). Las tareas con hora usan un bloque de 15 minutos desde su vencimiento; las que solo tienen fecha se muestran todo el día.", style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
                TextButton(onClick = onRefresh, enabled = !state.busy) {
                    Text("Actualizar tareas", style = PaquitoTypography.BodyMedium)
                }
            }
            item("past") {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Mostrar tareas vencidas", modifier = Modifier.weight(1f).padding(top = 12.dp), style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextPrimary)
                    Switch(checked = state.showPast, onCheckedChange = onShowPast, enabled = !state.busy)
                }
                Text("Las vencidas se envían con su fecha original y solo si las seleccionas.", style = PaquitoTypography.Caption, color = PaquitoColors.TextSecondary)
                if (state.skipped > 0) Text("${state.skipped} tareas omitidas por fecha ausente o inválida.", style = PaquitoTypography.Caption, color = PaquitoColors.TextSecondary)
            }
            state.message?.let { message -> item("message") {
                Text(message, modifier = Modifier.fillMaxWidth().paquitoSurface().padding(14.dp), style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextPrimary)
            } }
            state.report?.let { report -> item("report") {
                Column(Modifier.fillMaxWidth().paquitoSurface().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(if (report.error == null) "Sincronización completada" else "Sincronización incompleta", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
                    Text("${report.created} creados · ${report.updated} actualizados · ${report.unchanged} sin cambios", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                    if (report.failed > 0 || report.remaining > 0) Text("${report.failed} sin confirmar · ${report.remaining} pendientes de intentar. Puedes reintentar sin duplicar los ya enviados.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                }
            } }
            item("heading") { Text("Elige tus entregas", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary) }
            if (visible.isEmpty() && state.phase != CalendarPhase.Loading) item("empty") {
                Text("No hay tareas futuras pendientes con fecha válida. Puedes mostrar las vencidas para consultar los datos de demostración.", style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextSecondary)
            }
            items(visible, key = { it.key }) { candidate ->
                Row(Modifier.fillMaxWidth().paquitoSurface().padding(10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Checkbox(checked = candidate.key in state.selected, onCheckedChange = { onSelect(candidate.key) }, enabled = !state.busy)
                    Column(Modifier.weight(1f).padding(vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(candidate.task.title, style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
                        Text(candidate.task.course, style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
                        Text(candidateLabel(candidate), style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                        if (candidate.past) Text("Vencida · se conservará esta fecha", style = PaquitoTypography.Caption, color = PaquitoColors.TextSecondary)
                    }
                }
            }
        }
        PaquitoPrimaryButton("Sincronizar ${state.selected.size} tareas", onSync,
            enabled = !state.busy && state.email != null && state.selected.isNotEmpty(), isLoading = state.phase == CalendarPhase.Syncing)
        Spacer(Modifier.height(8.dp))
    }
}
private fun candidateLabel(candidate: CalendarCandidate): String = if (candidate.allDay) "${candidate.start} · todo el día"
    else DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm").withZone(CalendarEventPlanner.academicZone).format(Instant.parse(candidate.start))
