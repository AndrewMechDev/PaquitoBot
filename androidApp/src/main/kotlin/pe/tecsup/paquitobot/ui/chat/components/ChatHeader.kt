package pe.tecsup.paquitobot.ui.chat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Header del Chat (Figma 285:361) con glassmorphism.
 *
 * Estructura:
 *   [Avatar bot 46x46 gradient] [Nombre + estado] [Chip LMS]
 *
 * Estilo:
 *   - Fondo: backdrop-blur 13dp sobre rgba(255,255,255,0.78) + border-bottom.
 *   - Avatar: gradient turquesa con shadow fuerte.
 *   - Estado: dot verde 6x6 + texto "sincronizado hace 2 min".
 */
@Composable
fun ChatHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(PaquitoColors.SurfaceGlassStrong.copy(alpha = 0.78f))
            .border(
                width = 1.dp,
                color = androidx.compose.ui.graphics.Color(0x120D1520),
                shape = RoundedCornerShape(0.dp),
            )
            .padding(start = 18.dp, end = 18.dp, top = 58.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BotAvatar()
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = "Paquito",
                style = pe.tecsup.paquitobot.ui.theme.PaquitoTypography.Greeting.copy(
                    fontSize = 19.sp,
                ),
                color = PaquitoColors.TextOnCardStrong,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(PaquitoColors.StateSuccess),
                )
                Text(
                    text = "sincronizado hace 2 min",
                    fontSize = 11.sp,
                    color = PaquitoColors.StateSuccess,
                    fontFamily = pe.tecsup.paquitobot.ui.theme.PaquitoFont.InstrumentSans,
                )
            }
        }
        LmsChip()
    }
}

@Composable
private fun BotAvatar() {
    val gradient = Brush.linearGradient(
        colors = listOf(
            androidx.compose.ui.graphics.Color(0xFF4FD6FF),
            androidx.compose.ui.graphics.Color(0xFF0393C9),
        ),
        start = androidx.compose.ui.geometry.Offset(0f, 0f),
        end = androidx.compose.ui.geometry.Offset(46f, 46f),
    )
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(17.dp),
            )
            .clip(RoundedCornerShape(17.dp))
            .background(gradient),
    )
}

@Composable
private fun LmsChip() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(androidx.compose.ui.graphics.Color(0xFFF2F4F6))
            .border(1.dp, androidx.compose.ui.graphics.Color(0x100D1520), RoundedCornerShape(12.dp))
            .padding(horizontal = 11.dp, vertical = 7.dp),
    ) {
        Text(
            text = "LMS",
            fontSize = 11.sp,
            fontFamily = pe.tecsup.paquitobot.ui.theme.PaquitoFont.InstrumentSans,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
            color = PaquitoColors.TextOnCardMuted,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
private fun ChatHeaderPreview() {
    PaquitoTheme {
        ChatHeader()
    }
}