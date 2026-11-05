package pe.tecsup.paquitobot.ui.academic

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Tab
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.domain.academic.AcademicRepository
import pe.tecsup.paquitobot.domain.academic.AcademicRiskAlert
import pe.tecsup.paquitobot.domain.academic.AcademicRiskEngine
import pe.tecsup.paquitobot.domain.academic.AcademicRiskKind
import pe.tecsup.paquitobot.domain.academic.AcademicRiskLevel
import pe.tecsup.paquitobot.ui.components.AcademicErrorBanner
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoSpacing
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.paquitoSurface

data class AcademicRiskScreenData(
    val alerts: List<AcademicRiskAlert> = emptyList(),
) {
    val criticalCount: Int get() = alerts.count { it.level == AcademicRiskLevel.Critical || it.level == AcademicRiskLevel.Urgent }
    val watchCount: Int get() = alerts.count { it.level == AcademicRiskLevel.Watch }
}

/** Estado de UI para el tablero de alerta temprana. El motor queda en sharedLogic para iOS. */
class AcademicRiskViewModel(
    private val repository: AcademicRepository,
) : AcademicLoadingViewModel<AcademicRiskScreenData>(AcademicRiskScreenData()) {
    init { load() }

    override suspend fun fetch(current: AcademicRiskScreenData): Result<AcademicRiskScreenData> =
        repository.loadAllCourseRecords().map { courses ->
            AcademicRiskScreenData(
                alerts = courses.flatMap { (course, records) ->
                    AcademicRiskEngine.assess(course, records.grades, records.attendance, records.sessions).alerts
                }.sortedWith(compareByDescending<AcademicRiskAlert> { it.level.ordinal }.thenBy { it.courseCode }),
            )
        }
}

@Composable
fun AcademicRiskScreen(
    viewModel: AcademicRiskViewModel,
    onBackClick: () -> Unit,
    onContactTutor: (AcademicRiskAlert) -> Unit,
    onOpenCourse: (AcademicRiskAlert) -> Unit = {},
    focusCourseId: Int? = null,
    initialKind: AcademicRiskKind = AcademicRiskKind.Grade,
    modifier: Modifier = Modifier,
) {
    val data by viewModel.uiState.collectAsState()
    val error by viewModel.errorMessage.collectAsState()
    AcademicRiskScreen(
        data = if (focusCourseId == null) data else AcademicRiskScreenData(data.alerts.filter { it.courseId == focusCourseId }),
        errorMessage = error,
        onBackClick = onBackClick,
        onRetry = viewModel::load,
        onContactTutor = onContactTutor,
        onOpenCourse = onOpenCourse,
        initialKind = initialKind,
        modifier = modifier,
    )
}

@Composable
fun AcademicRiskScreen(
    data: AcademicRiskScreenData,
    errorMessage: String? = null,
    onBackClick: () -> Unit = {},
    onRetry: () -> Unit = {},
    onContactTutor: (AcademicRiskAlert) -> Unit = {},
    onOpenCourse: (AcademicRiskAlert) -> Unit = {},
    initialKind: AcademicRiskKind = AcademicRiskKind.Grade,
    modifier: Modifier = Modifier,
) {
    var selectedKind by remember(initialKind) { mutableStateOf(initialKind) }
    BackHandler(onBack = onBackClick)
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = PaquitoSpacing.lg)
            .padding(top = 8.dp, bottom = PaquitoSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        IconButton(onClick = onBackClick) {
            androidx.compose.foundation.Image(
                painter = painterResource(R.drawable.ic_paquito_arrow_back),
                contentDescription = "Volver",
                modifier = Modifier.size(21.dp),
                contentScale = ContentScale.Fit,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Mi seguimiento", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextHomeStrong)
            Text(
                "Entiende cómo vas y qué puedes mejorar en cada curso.",
                style = PaquitoTypography.BodyLarge,
                color = PaquitoColors.TextHomeMuted,
            )
        }
        RiskSummary(data)
        PrimaryTabRow(selectedTabIndex = selectedKind.ordinal, containerColor = PaquitoColors.Background,
            contentColor = PaquitoColors.StateInfoStrong) {
            listOf("Notas", "Asistencias").forEachIndexed { index, label ->
                Tab(selected = selectedKind.ordinal == index, onClick = { selectedKind = AcademicRiskKind.entries[index] },
                    text = { Text(label, style = PaquitoTypography.TaskTitle) })
            }
        }
        if (errorMessage != null) AcademicErrorBanner(message = errorMessage, onRetry = onRetry)
        if (data.alerts.none { it.kind == selectedKind } && errorMessage == null) {
            EmptyRiskState()
        } else {
            data.alerts.filter { it.kind == selectedKind }.forEach { alert ->
                AcademicRiskCard(alert = alert, onContactTutor = { onContactTutor(alert) }, onOpenCourse = { onOpenCourse(alert) })
            }
        }
    }
}

@Composable
private fun RiskSummary(data: AcademicRiskScreenData) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
        SummaryTile(
            value = data.criticalCount.toString(),
            label = "requieren acción",
            accent = PaquitoColors.StateDanger,
            modifier = Modifier.weight(1f),
        )
        SummaryTile(
            value = data.watchCount.toString(),
            label = "para vigilar",
            accent = PaquitoColors.StateWarning,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SummaryTile(value: String, label: String, accent: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .paquitoSurface(PaquitoShapes.large)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(value, style = PaquitoTypography.AlertCardTitle, color = accent)
        Text(label, style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
    }
}

@Composable
private fun AcademicRiskCard(alert: AcademicRiskAlert, onContactTutor: () -> Unit, onOpenCourse: () -> Unit) {
    var expanded by remember(alert.courseId, alert.kind) { mutableStateOf(true) }
    val accent = when (alert.level) {
        AcademicRiskLevel.Critical, AcademicRiskLevel.Urgent -> PaquitoColors.StateDanger
        AcademicRiskLevel.Watch -> PaquitoColors.StateWarning
        AcademicRiskLevel.Stable -> PaquitoColors.StateSuccess
    }
    val isAttendance = alert.kind == AcademicRiskKind.Attendance
    val progress = (alert.currentValue / alert.limitValue).toFloat().coerceIn(0f, 1f)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .paquitoSurface(PaquitoShapes.card)
            .animateContentSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                .background(PaquitoColors.OverlayInfoSubtle),
                contentAlignment = Alignment.Center,
            ) {
                androidx.compose.foundation.Image(painterResource(if (isAttendance) R.drawable.ic_paquito_frame_person else R.drawable.ic_paquito_docs_default),
                    contentDescription = null, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    alert.courseCode,
                    style = PaquitoTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                    color = accent,
                )
                Text(alert.courseName, style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextOnCardStrong, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Text(alert.title, style = PaquitoTypography.TaskTitle, color = accent)
        Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (isAttendance) "Faltas registradas" else "Proyección actual", style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
                Text(
                    if (isAttendance) "${alert.currentValue.toInt()} / ${alert.limitValue.toInt()}" else "${formatScore(alert.currentValue)} / ${alert.limitValue.toInt()}",
                    style = PaquitoTypography.Caption.copy(fontWeight = FontWeight.SemiBold),
                    color = PaquitoColors.TextOnCardStrong,
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
                color = accent,
                trackColor = PaquitoColors.Background,
            )
        }
        HorizontalDivider(color = PaquitoColors.TextOnCardMuted.copy(alpha = 0.12f))
        Row(Modifier.fillMaxWidth().clickable { expanded = !expanded }.padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("¿Por qué recibí este aviso?", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextHomeStrong)
            Text(if (expanded) "−" else "+", style = PaquitoTypography.TaskTitle, color = PaquitoColors.StateInfoStrong)
        }
        if (expanded) {
            Text(alert.message, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
            Text(if (isAttendance) "Revisa las fechas de tus inasistencias y organiza tu asistencia a las siguientes clases. Si hay un registro incorrecto, consulta con tu tutora."
                else "Revisa tus evaluaciones y prioriza las que faltan. Tu tutora puede ayudarte a organizar un plan de estudio y aclarar tus alternativas.",
                style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
        }
        PaquitoPrimaryButton(text = if (isAttendance) "Ver mis asistencias" else "Ver mis notas", onClick = onOpenCourse)
        TextButton(onClick = onContactTutor, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Consultar con mi tutora", style = PaquitoTypography.BodyMedium, color = PaquitoColors.StateInfoStrong)
        }
    }
}

@Composable
private fun EmptyRiskState() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 56.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Box(
            modifier = Modifier.size(56.dp).clip(CircleShape).background(PaquitoColors.OverlayInfoSubtle),
            contentAlignment = Alignment.Center,
        ) { Text("✓", style = PaquitoTypography.AlertCardTitle, color = PaquitoColors.StateSuccess) }
        Text("Vas por buen camino", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextOnCardStrong)
        Text("No detectamos riesgos con los datos actuales.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
    }
}

private fun formatScore(value: Double): String = "%.1f".format(value).replace(',', '.')
