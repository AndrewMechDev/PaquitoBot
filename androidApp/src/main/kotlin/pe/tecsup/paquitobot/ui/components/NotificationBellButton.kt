package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Boton de campana con badge de no leidas, para el header del Home.
 *
 * Mismo lenguaje visual que el badge del FAB de Paquito en [Navbar]
 * (circulo `StateDanger` + borde blanco 2.5dp + numero blanco bold), pero
 * a escala de icono chico en vez del FAB de 60dp.
 *
 * Toque minimo 44dp (icono real 22dp, resto es zona de toque) para
 * accesibilidad, aunque el diseño visual sea mas chico.
 */
@Composable
fun NotificationBellButton(
    unreadCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.size(44.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(PaquitoColors.SurfaceElevated)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_paquito_bell_outline),
                contentDescription = if (unreadCount > 0) {
                    "Notificaciones, $unreadCount sin leer"
                } else {
                    "Notificaciones"
                },
                modifier = Modifier.size(22.dp),
                contentScale = ContentScale.Fit,
            )
        }
        if (unreadCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 2.dp, y = (-2).dp)
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(PaquitoColors.StateDanger)
                    .border(2.dp, Color(0xFFFAFBFC), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (unreadCount > 9) "9+" else unreadCount.toString(),
                    fontSize = 9.sp,
                    fontFamily = PaquitoFont.DMSans,
                    fontWeight = FontWeight.Bold,
                    color = PaquitoColors.TextOnWhite,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationBellButtonPreview() {
    PaquitoTheme {
        NotificationBellButton(unreadCount = 2, onClick = {})
    }
}
