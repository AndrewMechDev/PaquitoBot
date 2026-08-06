package pe.tecsup.paquitobot.ui.chat.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
 * Estado de sincronizacion con el LMS. Por ahora es solo visual (sin logica
 * real de red/polling) - el ViewModel lo actualizara cuando exista backend.
 */
enum class SyncStatus { Synced, Syncing, Offline }

/**
 * Header del Chat, fiel al frame Figma `438:662`.
 *
 * Reemplaza el header glassmorphism anterior (avatar + chip LMS, Figma
 * 285:361, iteracion vieja) por el patron simple del frame actual:
 *   [flecha atras]
 *   "Hola, {nombre}"
 *   "Puedes consultarme lo que quieras"
 *   [chip de estado de sincronizacion]
 *
 * El chip de estado fusiona dos cosas que antes eran elementos separados:
 * el divisor "HOY" (ya no existe como pieza propia) y el indicador de
 * sincronizacion del header viejo ("sincronizado hace 2 min") - a pedido
 * del usuario, para no perder esa señal util.
 */
@Composable
fun ChatHeader(
    userName: String,
    syncStatus: SyncStatus,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 58.dp, bottom = 0.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_arrow_back),
            contentDescription = "Volver",
            modifier = Modifier
                .size(21.dp)
                .clickable(onClick = onBackClick),
            contentScale = ContentScale.Fit,
        )
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "Hola, $userName",
                fontSize = 36.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeStrong,
            )
            Text(
                text = "Puedes consultarme lo que quieras",
                fontSize = 20.sp,
                fontFamily = PaquitoFont.DMSans,
                color = PaquitoColors.TextHomeMuted,
            )
            SyncStatusChip(status = syncStatus)
        }
    }
}

@Composable
private fun SyncStatusChip(status: SyncStatus) {
    val (dotColor, label) = when (status) {
        SyncStatus.Synced  -> PaquitoColors.StateSuccess to "Sincronizado con LMS"
        SyncStatus.Syncing -> PaquitoColors.StateInfo to "Sincronizando..."
        SyncStatus.Offline -> PaquitoColors.StateDanger to "Sin conexión"
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0x120D1520))
            .padding(horizontal = 11.dp, vertical = 5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor),
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontFamily = PaquitoFont.DMMono,
            color = PaquitoColors.TextOnCardMuted,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatHeaderPreview() {
    PaquitoTheme {
        Column {
            ChatHeader(userName = "{nombre}", syncStatus = SyncStatus.Synced, onBackClick = {})
            ChatHeader(userName = "{nombre}", syncStatus = SyncStatus.Syncing, onBackClick = {})
            ChatHeader(userName = "{nombre}", syncStatus = SyncStatus.Offline, onBackClick = {})
        }
    }
}
