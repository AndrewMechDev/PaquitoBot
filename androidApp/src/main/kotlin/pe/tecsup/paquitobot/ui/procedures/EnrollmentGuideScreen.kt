package pe.tecsup.paquitobot.ui.procedures

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import pe.tecsup.paquitobot.PaquitoBotApp
import pe.tecsup.paquitobot.domain.procedures.*
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.*

/** Solo contenido público. Nunca permite entrar a cursos, sesión o notificaciones sin Clerk. */
@Composable
fun EnrollmentGuideRoute(onBackClick: () -> Unit) {
    val repository = (LocalContext.current.applicationContext as PaquitoBotApp).procedureRepository
    val model: ProcedureViewModel = viewModel(factory = remember(repository) {
        viewModelFactory { initializer { ProcedureViewModel(repository) } }
    })
    val state by model.uiState.collectAsStateWithLifecycle()
    var relatedId by rememberSaveable { mutableStateOf<String?>(null) }
    val holder = rememberSaveableStateHolder()
    BackHandler(onBack = onBackClick)
    val ready = state as? ProcedureUiState.Ready
    val related = ready?.catalog?.find(relatedId)
    if (related != null) {
        ProcedureDetailScreen(related, ready.catalog.source, onBackClick = { relatedId = null })
    } else if (ready != null && ready.catalog.enrollment != null) {
        holder.SaveableStateProvider("enrollment-guide") {
            EnrollmentGuideScreen(ready.catalog, onBackClick = onBackClick, onRelatedClick = { relatedId = it })
        }
    } else {
        Column(Modifier.fillMaxSize().background(PaquitoColors.Background).systemBarsPadding().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)) {
            GuideHeader("Matrícula", "Guía paso a paso", onBackClick)
            if (state == ProcedureUiState.Loading) CircularProgressIndicator(color = PaquitoColors.StateInfoStrong)
            else {
                Text("No pudimos cargar la guía de matrícula.", style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextPrimary)
                PaquitoPrimaryButton("Reintentar", model::load)
            }
        }
    }
}

@Composable
internal fun EnrollmentGuideScreen(catalog: ProcedureCatalog, onBackClick: () -> Unit, onRelatedClick: (String) -> Unit) {
    val guide = requireNotNull(catalog.enrollment)
    var trackId by rememberSaveable { mutableStateOf(guide.tracks.first().id) }
    var stage by rememberSaveable { mutableIntStateOf(-1) }
    var calendarOpen by rememberSaveable { mutableStateOf(false) }
    val track = guide.tracks.firstOrNull { it.id == trackId } ?: guide.tracks.first()
    val steps = guide.stepsFor(track)
    val current = stage.coerceIn(-1, steps.size)
    fun back() { if (current >= 0) stage = current - 1 else onBackClick() }
    BackHandler { back() }
    Column(Modifier.fillMaxSize().background(PaquitoColors.Background).systemBarsPadding().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuideHeader("Matrícula", "Tu recorrido · Arequipa", ::back)
        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            guide.tracks.forEach { option ->
                GuideCategoryChip(option.title, track.id == option.id) { if (trackId != option.id) { trackId = option.id; stage = -1 } }
            }
        }
        LinearProgressIndicator(progress = { (current + 1).toFloat() / (steps.size + 1) },
            modifier = Modifier.fillMaxWidth(), color = PaquitoColors.StateInfoStrong, trackColor = PaquitoColors.SurfaceElevated)
        Text(if (current < 0) "Antes de empezar" else if (current == steps.size) "Recorrido revisado" else "Paso ${current + 1} de ${steps.size} · ${track.description}",
            style = PaquitoTypography.Caption, color = PaquitoColors.TextSecondary)
        AnimatedContent(targetState = track.id to current, transitionSpec = { fadeIn() togetherWith fadeOut() },
            modifier = Modifier.weight(1f), label = "enrollment-step") { (targetTrack, targetStage) ->
            val shownTrack = guide.tracks.first { it.id == targetTrack }
            val shownSteps = guide.stepsFor(shownTrack)
            key(targetTrack, targetStage) {
                Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    when {
                        targetStage < 0 -> {
                            Text("Prepara tu matrícula", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary)
                            SourceNote()
                            GuideSection("Comprueba antes de empezar", shownTrack.checklist)
                            Text("Puedes consultar esta guía sin iniciar sesión. No registra pagos ni confirma matrícula.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                            Text("¿Necesitas otro trámite?", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
                            shownTrack.relatedProcedureIds.mapNotNull(catalog::find).forEach { procedure ->
                                OutlinedButton(onClick = { onRelatedClick(procedure.id) }, modifier = Modifier.fillMaxWidth(), shape = PaquitoShapes.medium) {
                                    Text(procedure.title, style = PaquitoTypography.BodyMedium)
                                }
                            }
                        }
                        targetStage == shownSteps.size -> {
                            Text("Ya revisaste los pasos", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary)
                            Text("Tu matrícula se confirma únicamente en el portal de Tecsup. Llegar aquí no acredita un pago ni una inscripción.", style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextSecondary)
                            guide.steps.first { it.id == "confirmation" }.channels.forEach {
                                GuideChannelAction(it, "Consulta de matrícula")
                            }
                        }
                        else -> {
                            val step = shownSteps[targetStage]
                            if (step.optional) Text("Solo si necesitas cambios", style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
                            Text(step.title, style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary)
                            GuideSection("Qué debes hacer", step.body, numbered = true)
                            if (step.cost != null || step.responseTime != null) {
                                Column(Modifier.fillMaxWidth().paquitoSurface().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    step.cost?.let { GuideFact("Costo de referencia · 2024", it) }
                                    step.responseTime?.let { GuideFact("Plazo indicado en el manual", it) }
                                }
                            }
                            step.channels.forEach { channel -> GuideChannelAction(channel, "Consulta: " + step.title) }
                            Text("PE-003 · MPA de referencia 2024 · página ${step.sourcePage}. Confirma las condiciones actuales con Tecsup.", style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
                            GuideChannelAction(ProcedureChannel("Ver fuente de este paso", catalog.source.url + "#page=" + step.sourcePage, ProcedureChannelKind.Web), "Consulta de matrícula")
                        }
                    }
                    TextButton(onClick = { calendarOpen = !calendarOpen }) {
                        Text(if (calendarOpen) "Ocultar fechas y plazos" else "Consultar fechas y plazos", style = PaquitoTypography.BodyMedium)
                    }
                    if (calendarOpen) {
                        Column(Modifier.fillMaxWidth().paquitoSurface().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(guide.calendar.period ?: "Cronograma por confirmar", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
                            GuideFact("Matrícula regular", guide.calendar.regular ?: "Sin fecha confirmada en esta guía")
                            GuideFact("Matrícula extraordinaria", guide.calendar.extraordinary ?: "Sin fecha confirmada en esta guía")
                            GuideFact("Inicio de clases", guide.calendar.classesStart ?: "Sin fecha confirmada en esta guía")
                            Text("Solicita el cronograma de tu sede y período a Servicios Educativos. Las fechas dependen de tu sede y período académico.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                            GuideChannelAction(guide.calendar.officialChannel, "Consulta de cronograma de matrícula")
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (current >= 0) OutlinedButton(onClick = { back() }, modifier = Modifier.weight(1f), shape = PaquitoShapes.medium) {
                Text("Anterior", style = PaquitoTypography.BodyMedium)
            }
            PaquitoPrimaryButton(text = when { current < 0 -> "Ver pasos"; current == steps.size -> "Revisar de nuevo"; current == steps.lastIndex -> "Finalizar lectura"; else -> "Siguiente" },
                onClick = { stage = if (current == steps.size) -1 else current + 1 }, modifier = Modifier.weight(1.6f))
        }
        Spacer(Modifier.height(8.dp))
    }
}
