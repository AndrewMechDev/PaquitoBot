package pe.tecsup.paquitobot.ui.procedures

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import pe.tecsup.paquitobot.domain.procedures.ProcedureChannel
import pe.tecsup.paquitobot.domain.procedures.ProcedureChannelKind
import pe.tecsup.paquitobot.ui.theme.*

/** Canal compartido por secretaría y matrícula: prepara correo o abre web, nunca envía. */
@Composable
internal fun GuideChannelAction(channel: ProcedureChannel, subject: String) {
    val context = LocalContext.current
    var error by remember(channel, subject) { mutableStateOf<String?>(null) }
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Button(onClick = {
            error = null
            val intent = if (channel.kind == ProcedureChannelKind.Email) Intent(Intent.ACTION_SENDTO,
                ("mailto:" + Uri.encode(channel.target) + "?subject=" + Uri.encode(subject)).toUri())
            else Intent(Intent.ACTION_VIEW, channel.target.toUri())
            try { context.startActivity(intent) }
            catch (_: ActivityNotFoundException) { error = "No hay una aplicación disponible. Puedes copiar la dirección de abajo." }
            catch (_: SecurityException) { error = "No se pudo abrir el canal. Puedes copiar la dirección de abajo." }
        }, modifier = Modifier.fillMaxWidth(), shape = PaquitoShapes.medium,
            colors = ButtonDefaults.buttonColors(containerColor = PaquitoColors.BrandPrimary, contentColor = PaquitoColors.TextOnPrimaryDim)) {
            Text(channel.label, style = PaquitoTypography.ButtonLabel)
        }
        SelectionContainer { Text(channel.target, style = PaquitoTypography.BodySmall, color = PaquitoColors.TextOnCardMuted) }
        error?.let { Text(it, style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextPrimary) }
    }
}
