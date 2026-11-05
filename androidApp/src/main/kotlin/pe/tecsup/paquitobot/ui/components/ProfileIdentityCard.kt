package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography
import pe.tecsup.paquitobot.ui.theme.paquitoSurface

@Composable
internal fun ProfileIdentityCard(name: String, email: String, subtitle: String = "Cuenta institucional") {
    Column(
        modifier = Modifier.fillMaxWidth().paquitoSurface().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(Modifier.size(76.dp).background(PaquitoColors.OverlayInfoSubtle, CircleShape), contentAlignment = Alignment.Center) {
            Text(
                name.trim().split(Regex("\\s+")).take(2).mapNotNull { it.firstOrNull() }.joinToString(" ").uppercase(),
                style = PaquitoTypography.HeadlineSmall,
                color = PaquitoColors.StateInfoStrong,
            )
        }
        Text(name, style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextHomeStrong)
        Text(email, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
        Text(subtitle, style = PaquitoTypography.Caption, color = PaquitoColors.StateInfoStrong)
    }
}
