package pe.tecsup.paquitobot.ui.procedures

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.domain.procedures.*
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
import pe.tecsup.paquitobot.ui.theme.*

/** Acceso al catálogo desde Perfil; comparte los tokens de las tarjetas de eventos. */
@Composable
fun ProcedureEntryCard(onClick: () -> Unit, modifier: Modifier = Modifier,
                       title: String = "Trámites de secretaría", subtitle: String = "Requisitos, pasos y canales de atención",
                       iconRes: Int = R.drawable.ic_paquito_docs_default) {
    Row(modifier.fillMaxWidth().paquitoSurface().clickable(onClick = onClick).padding(18.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Image(painterResource(iconRes), contentDescription = null, modifier = Modifier.size(32.dp))
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextOnCardStrong)
            Text(subtitle, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
        }
        Text("›", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.StateInfoStrong)
    }
}

@Composable
fun ProcedureGuideScreen(state: ProcedureUiState, onRetry: () -> Unit, onProcedureClick: (String) -> Unit,
                         onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBackClick)
    var query by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf<ProcedureCategory?>(null) }
    Column(modifier.fillMaxSize().background(PaquitoColors.Background).systemBarsPadding().padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)) {
        GuideHeader("Secretaría", "Guía de trámites · Arequipa", onBackClick)
        when (state) {
            ProcedureUiState.Loading -> Box(Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PaquitoColors.StateInfoStrong)
            }
            ProcedureUiState.Error -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("No pudimos cargar la guía. Intenta nuevamente.", style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextPrimary)
                PaquitoPrimaryButton("Reintentar", onRetry)
            }
            is ProcedureUiState.Ready -> {
                val catalog = state.catalog
                OutlinedTextField(value = query, onValueChange = { query = it }, modifier = Modifier.fillMaxWidth(),
                    singleLine = true, shape = PaquitoShapes.large, textStyle = PaquitoTypography.BodyLarge,
                    label = { Text("Buscar trámite o código", style = PaquitoTypography.BodyMedium) },
                    trailingIcon = { if (query.isNotEmpty()) TextButton(onClick = { query = "" }) {
                        Text("Limpiar", style = PaquitoTypography.Caption)
                    } })
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    GuideCategoryChip("Todos", category == null) { category = null }
                    ProcedureCategory.entries.filter { value -> catalog.procedures.any { it.category == value } }.forEach { value ->
                        GuideCategoryChip(value.label, category == value) { category = value }
                    }
                }
                val filtered = remember(catalog, query, category) { catalog.search(query, category) }
                Text("${filtered.size} trámites · información de referencia 2024", style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
                LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)) {
                    item(key = "source-note") { SourceNote() }
                    if (filtered.isEmpty()) item(key = "empty") {
                        Column(Modifier.padding(vertical = 24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("No encontramos ese trámite", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
                            Text("Prueba con otra palabra o consulta todas las categorías.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
                            TextButton(onClick = { query = ""; category = null }) {
                                Text("Ver todos", style = PaquitoTypography.BodyMedium)
                            }
                        }
                    }
                    items(filtered, key = { it.id }) { item ->
                        Column(Modifier.fillMaxWidth().paquitoSurface(PaquitoShapes.card)
                            .clickable { onProcedureClick(item.id) }.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("${item.code} · ${item.category.label}", style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
                            Text(item.title, style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextOnCardStrong)
                            Text(item.summary, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
                            Text("${item.cost} · referencial", style = PaquitoTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.TextOnCardStrong)
                            Text("Ver requisitos y pasos  ›", style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun GuideCategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = selected, onClick = onClick, label = {
        Text(label, style = PaquitoTypography.BodyMedium)
    }, colors = FilterChipDefaults.filterChipColors(
        containerColor = PaquitoColors.SurfaceGlassSoft, labelColor = PaquitoColors.TextOnCardMuted,
        selectedContainerColor = PaquitoColors.OverlayInfoSubtle, selectedLabelColor = PaquitoColors.StateInfoStrong))
}

@Composable
fun ProcedureDetailScreen(procedure: AcademicProcedure, source: ProcedureSource, onBackClick: () -> Unit,
                          modifier: Modifier = Modifier) {
    BackHandler(onBack = onBackClick)
    Column(modifier.fillMaxSize().background(PaquitoColors.Background).systemBarsPadding()
        .verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        GuideHeader("Tu trámite", procedure.category.label, onBackClick)
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(procedure.code, style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
            Text(procedure.title, style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary)
            Text(procedure.summary, style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextSecondary)
        }
        SourceNote()
        Column(Modifier.fillMaxWidth().paquitoSurface().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            GuideFact("Dirigido a", procedure.audience)
            GuideFact("Costo referencial · consulta las notas de vigencia", procedure.cost)
            GuideFact("Tiempo de respuesta indicado", procedure.responseTime)
            procedure.applicationWindow?.let { GuideFact("Cuándo solicitarlo", it) }
            procedure.duration?.let { GuideFact("Duración del programa", it) }
            GuideFact("Área responsable", procedure.responsibleArea)
        }
        GuideSection("Requisitos", procedure.requirements)
        GuideSection("Cómo realizarlo", procedure.steps, numbered = true)
        if (procedure.notes.isNotEmpty()) GuideSection("Ten en cuenta", procedure.notes)
        Column(Modifier.fillMaxWidth().paquitoSurface().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Canales de atención", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
            Text("Los enlaces abren servicios externos. Si eliges correo, revisa y envía tu consulta desde tu aplicación.", style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
            procedure.channels.forEach { channel -> GuideChannelAction(channel, "Consulta: " + procedure.title) }
        }
        Text("Fuente y vigencia", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
        Text("${source.title} · ${source.edition} · página ${procedure.sourcePage}.\nContenido contrastado: ${source.reviewedOn}. Esta fecha no confirma la vigencia de tarifas o contactos.",
            style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
        GuideChannelAction(ProcedureChannel("Consultar manual oficial", source.url + "#page=" + procedure.sourcePage,
            ProcedureChannelKind.Web), "Consulta: " + procedure.title)
        Spacer(Modifier.height(12.dp))
    }
}

// Secciones específicas de una guía; las tarjetas y tipografía reutilizan el theme existente.
@Composable
internal fun GuideHeader(title: String, subtitle: String, onBack: () -> Unit, backDescription: String = "Volver") {
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
            Image(painterResource(R.drawable.ic_paquito_arrow_back), backDescription, Modifier.size(24.dp))
        }
        Column(Modifier.weight(1f).padding(start = 8.dp)) {
            Text(title, style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextPrimary)
            Text(subtitle, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
        }
    }
}
@Composable
internal fun SourceNote() {
    Column(Modifier.fillMaxWidth().paquitoSurface().padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("Antes de realizar tu trámite", style = PaquitoTypography.BodyMedium.copy(fontWeight = FontWeight.SemiBold), color = PaquitoColors.StateInfoStrong)
        Text("Requisitos basados en el manual de referencia 2024. Incluye acceso al tarifario oficial 2026; las notas de cada trámite indican los importes contrastados. Confirma fechas y condiciones con Tecsup antes de solicitarlo.",
            style = PaquitoTypography.BodySmall, color = PaquitoColors.TextOnCardMuted)
    }
}
@Composable
internal fun GuideFact(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = PaquitoTypography.Caption, color = PaquitoColors.TextOnCardMuted)
        Text(value, style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextOnCardStrong)
    }
}
@Composable
internal fun GuideSection(title: String, lines: List<String>, numbered: Boolean = false) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextPrimary)
        lines.forEachIndexed { index, line ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (numbered) "${index + 1}." else "•", style = PaquitoTypography.BodyLarge, color = PaquitoColors.StateInfoStrong)
                Text(line, modifier = Modifier.weight(1f), style = PaquitoTypography.BodyLarge, color = PaquitoColors.TextSecondary)
            }
        }
    }
}
@Preview(showBackground = true, name = "Secretaría · Inicio")
@Composable
private fun ProcedureEntryPreview() { PaquitoTheme { ProcedureEntryCard(onClick = {}) } }

// Datos exclusivos de preview: el contenido de producción proviene de procedures.json.
private val previewProcedure = AcademicProcedure(
    id = "constancia-estudios", code = "PE-012", title = "Constancia de estudios",
    category = ProcedureCategory.Documents, audience = "Estudiantes",
    summary = "Acredita tu situación académica mediante una constancia.",
    requirements = listOf("Cumplir los requisitos formales de Servicios Educativos.", "Pagar el derecho correspondiente.", "No tener deudas vencidas del período solicitado."),
    steps = listOf("Reúne los requisitos y confirma las condiciones actuales.", "Presenta la solicitud en Intranet y conserva la constancia de envío.", "Consulta la respuesta y las indicaciones de entrega."),
    cost = "S/ 31.50", responseTime = "7 días hábiles desde la solicitud",
    applicationWindow = null, duration = null, responsibleArea = "Secretaría Académica",
    channels = listOf(ProcedureChannel("Abrir Intranet Académica", "https://campus-cloud.tecsup.edu.pe/auth/login", ProcedureChannelKind.Web)),
    notes = listOf("La entrega puede ser por correo institucional o presencial."), sourcePage = 6,
)
private val previewSource = ProcedureSource("Manual de Procesos Académicos · TECSUP N.º 2",
    "https://www.tecsup.edu.pe/wp-content/uploads/2024/07/mpa-tecsup-2.pdf", "Referencia documental 2024", "2026-10-18")
@Preview(showBackground = true, name = "Secretaría · Catálogo", widthDp = 360, heightDp = 800)
@Composable
private fun ProcedureCatalogPreview() {
    PaquitoTheme {
        ProcedureGuideScreen(ProcedureUiState.Ready(ProcedureCatalog(previewSource, listOf(previewProcedure))), {}, {}, {})
    }
}
@Preview(showBackground = true, name = "Secretaría · Detalle", widthDp = 360, heightDp = 800)
@Composable
private fun ProcedureDetailPreview() {
    PaquitoTheme { ProcedureDetailScreen(previewProcedure, previewSource, {}) }
}
