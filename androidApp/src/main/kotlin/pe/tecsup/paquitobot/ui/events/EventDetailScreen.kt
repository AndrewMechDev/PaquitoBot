package pe.tecsup.paquitobot.ui.events

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.paquitoSurface

@Composable
fun EventDetailScreen(
    event: CampusEvent,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(PaquitoColors.Background)
            .systemBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(R.drawable.ic_paquito_arrow_back),
                contentDescription = "Volver a eventos",
                modifier = Modifier.size(24.dp).clickable(onClick = onBackClick),
            )
            Text(
                "Eventos",
                style = PaquitoTypography.BodyLarge,
                color = PaquitoColors.TextHomeMuted,
                modifier = Modifier.padding(start = 16.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(event.category.uppercase(), style = PaquitoTypography.LabelSmall, color = PaquitoColors.StateInfoStrong)
            Text(event.title, style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextHomeStrong)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .paquitoSurface(PaquitoShapes.card)
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            EventFact(label = "FECHA", value = event.dateRangeLabel)
            HorizontalDivider(color = PaquitoColors.BorderDefault)
            EventFact(label = "LUGAR", value = event.venue)
        }

        EventTextSection("¿De qué trata?", event.description)
        EventTextSection("¿Por qué podría interesarte?", event.whyWorthIt)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .paquitoSurface()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Inscripción", style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextOnCardStrong)
            Text(event.registrationNote, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardStrong)
            Button(
                onClick = { uriHandler.openUri(event.actionUrl) },
                modifier = Modifier.fillMaxWidth(),
                shape = PaquitoShapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaquitoColors.BrandPrimary,
                    contentColor = PaquitoColors.TextOnPrimaryDim,
                ),
            ) {
                Text(event.actionLabel, style = PaquitoTypography.ButtonLabel)
            }
        }

        Text(
            text = "Fuente: ${event.sourceName}",
            style = PaquitoTypography.Caption,
            color = PaquitoColors.TextLink,
            modifier = Modifier.clickable { uriHandler.openUri(event.sourceUrl) },
        )
    }
}

@Composable
private fun EventFact(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = PaquitoTypography.LabelSmall, color = PaquitoColors.TextHomeTaskLabel)
        Text(value, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardStrong)
    }
}

@Composable
private fun EventTextSection(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = PaquitoTypography.TaskTitle, color = PaquitoColors.TextHomeStrong)
        Text(body, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
    }
}

@Preview(showBackground = true)
@Composable
private fun EventDetailScreenPreview() {
    PaquitoTheme {
        EventDetailScreen(EventMockData.events.first(), onBackClick = {})
    }
}
