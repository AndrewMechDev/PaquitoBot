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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.paquitoSurface

@Composable
fun EventsScreen(
    onBackClick: () -> Unit,
    onEventClick: (CampusEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
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
                contentDescription = "Volver",
                modifier = Modifier.size(24.dp).clickable(onClick = onBackClick),
            )
            Column(Modifier.padding(start = 16.dp)) {
                Text("Eventos", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextHomeStrong)
                Text(
                    "Tecnología, investigación e innovación",
                    style = PaquitoTypography.BodyMedium,
                    color = PaquitoColors.TextHomeMuted,
                )
            }
        }

        Text(
            "Una selección para explorar, aprender y conectar con otras comunidades.",
            style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.TextOnCardMuted,
        )
        Text(
            "Eventos de muestra · información recopilada el 17 oct 2026",
            style = PaquitoTypography.Caption,
            color = PaquitoColors.TextHomeTaskLabel,
        )

        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            EventMockData.events.forEach { event ->
                EventSummaryCard(event = event, onClick = { onEventClick(event) })
            }
        }
    }
}

@Composable
private fun EventSummaryCard(event: CampusEvent, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .paquitoSurface(PaquitoShapes.card)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(PaquitoColors.OverlayInfoSubtle)
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                event.dateLabel.substringBefore(" ").uppercase(),
                style = PaquitoTypography.BodyLarge.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold),
                color = PaquitoColors.StateInfoStrong,
                maxLines = 1,
            )
            Text(
                event.dateLabel.substringAfter(" ", "" ).uppercase(),
                style = PaquitoTypography.Caption,
                color = PaquitoColors.TextOnCardMuted,
                maxLines = 1,
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(7.dp),
        ) {
            Text(
                event.category.uppercase(),
                style = PaquitoTypography.LabelSmall,
                color = PaquitoColors.StateInfoStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                event.title,
                style = PaquitoTypography.TaskTitle,
                color = PaquitoColors.TextOnCardStrong,
            )
            Text(
                event.venue,
                style = PaquitoTypography.Caption,
                color = PaquitoColors.TextOnCardMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                event.summary,
                style = PaquitoTypography.BodyMedium,
                color = PaquitoColors.TextOnCardStrong,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                "Ver detalles  ›",
                style = PaquitoTypography.ButtonLabel,
                color = PaquitoColors.TextLink,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EventsScreenPreview() {
    PaquitoTheme { EventsScreen(onBackClick = {}, onEventClick = {}) }
}
