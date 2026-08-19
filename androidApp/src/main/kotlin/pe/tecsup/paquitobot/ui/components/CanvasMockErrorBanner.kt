package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * Aviso de error uniforme cuando falla una llamada a `canvas-mock` (key
 * invalida, red caida, IP vieja, etc.) - antes las pantallas de datos
 * reales (Cursos/Horarios/Home) exponian `errorMessage` en su ViewModel
 * pero nadie lo mostraba: si canvas-mock se caia despues de conectar, la
 * pantalla se quedaba en "Cargando..." para siempre, sin aviso ni forma de
 * reintentar (bug real, encontrado en auditoria 2026-08-19).
 */
@Composable
fun CanvasMockErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(PaquitoShapes.large)
            .background(PaquitoColors.StateDanger.copy(alpha = 0.08f))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = message,
            style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.StateDanger,
        )
        Text(
            text = "Reintentar",
            style = PaquitoTypography.ButtonLabel,
            color = PaquitoColors.StateInfoStrong,
            modifier = Modifier.clickable(onClick = onRetry),
        )
    }
}
