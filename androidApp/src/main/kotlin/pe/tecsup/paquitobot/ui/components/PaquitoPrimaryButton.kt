package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoShapes
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/**
 * CTA primario del onboarding/gates (Figma `112:44`): 51dp, radio 20, `#00C9FB`.
 */
@Composable
fun PaquitoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        enabled = enabled && !isLoading,
        shape = PaquitoShapes.large,
        colors = ButtonDefaults.buttonColors(
            containerColor = PaquitoColors.BrandPrimary,
            contentColor = PaquitoColors.TextOnPrimary,
            disabledContainerColor = PaquitoColors.TextHomeMuted,
            disabledContentColor = PaquitoColors.TextOnPrimary,
        ),
        contentPadding = PaddingValues(horizontal = 15.dp, vertical = 15.dp),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = PaquitoColors.TextOnPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = PaquitoTypography.BodyLarge.copy(fontWeight = FontWeight.SemiBold),
                color = PaquitoColors.TextOnPrimary,
            )
        }
    }
}
