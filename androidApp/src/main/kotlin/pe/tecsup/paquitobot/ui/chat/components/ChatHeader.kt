package pe.tecsup.paquitobot.ui.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Header del Chat, fiel al frame Figma `438:662`.
 *
 *   [flecha atras]
 *   "Hola, {nombre}"
 *   "Puedes consultarme lo que quieras"
 *
 * Iteracion 2026-08-06: se saca el chip de estado de conexion/sincronizacion
 * (existio brevemente fusionado con el divisor "HOY" viejo). El usuario lo
 * encontro visualmente ruidoso ("rompe el diseño"). El estado de conexion
 * ahora se comunica DENTRO del flujo de mensajes (ver [pe.tecsup.paquitobot.ui.components.MessageRole.System]
 * en `Message.kt`) en vez de una pieza de UI fija en el header - mismo
 * patron que apps de chat consolidadas (ej. WhatsApp) usan para avisos de
 * "sin conexion" o "mensaje no entregado": aparecen como un item mas en la
 * conversacion, no como chrome permanente.
 *
 * Iteracion 2026-08-19 (auditoria de consistencia UI/UX): el saludo usaba
 * `fontSize = 28.sp` a mano, un tamaño DISTINTO al de cualquier otro
 * titulo grande de la app (Welcome/Canvas/Cursos/Horarios/Notificaciones
 * usan todos `PaquitoTypography.DisplayLarge`, 34sp). Mismo patron con el
 * subtitulo (`HeadlineSmall`, en vez de 16sp suelto). Se unifica al mismo
 * token para que la tipografia se sienta igual en toda la app.
 */
@Composable
fun ChatHeader(
    userName: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_arrow_back),
            contentDescription = "Volver",
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onBackClick),
            contentScale = ContentScale.Fit,
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Hola, $userName",
                style = PaquitoTypography.DisplayLarge,
                color = PaquitoColors.TextHomeStrong,
            )
            Text(
                text = "Puedes consultarme lo que quieras",
                style = PaquitoTypography.HeadlineSmall,
                color = PaquitoColors.TextHomeMuted,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatHeaderPreview() {
    PaquitoTheme {
        ChatHeader(userName = "Andrea", onBackClick = {})
    }
}
