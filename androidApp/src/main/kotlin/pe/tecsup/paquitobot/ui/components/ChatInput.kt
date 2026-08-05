package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Input inferior del Chat con boton de enviar (Figma 285:376-285:381).
 *
 *   [_____________input_______________] [Boton enviar]
 *
 * Estilo del input:
 *   - Border 1dp rgba(13,21,32,0.10), rounded 18dp, alto 50dp.
 *   - Background #F6F7F9 con shadow inset 0 1 3 1 rgba(13,21,32,0.05).
 *   - Placeholder Instrument Sans Regular 15sp color #757575.
 *
 * Estilo del boton enviar:
 *   - 50x50, gradient vertical #2AD0FF -> #0393C9, rounded 18dp.
 *   - Icono flecha (ic_paquito_send_arrow) rotado 45 grados, blanco.
 *   - Shadow 0 10 11 rgba(3,140,196,0.38) + inset white highlight 0 1 0 0 rgba(255,255,255,0.5).
 */
@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    placeholder: String = "Preguntale a Paquito...",
    onSend: (String) -> Unit = {},
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Input field.
        Row(
            modifier = Modifier
                .weight(1f)
                .height(50.dp)
                .shadow(
                    elevation = 0.dp,
                    shape = RoundedCornerShape(18.dp),
                )
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFF6F7F9))
                .border(1.dp, Color(0x1A0D1520), RoundedCornerShape(18.dp))
                .padding(horizontal = 17.dp, vertical = 14.5.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = placeholder,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        // Boton enviar.
        SendButton(onClick = {
            if (text.isNotBlank()) {
                onSend(text.trim())
                text = ""
            }
        })
    }
}

@Composable
private fun SendButton(onClick: () -> Unit) {
    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2AD0FF),
            Color(0xFF0393C9),
        ),
    )
    Row(
        modifier = Modifier
            .size(50.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(18.dp),
            )
            .clip(RoundedCornerShape(18.dp))
            .background(gradient)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_send_arrow),
            contentDescription = "Enviar mensaje",
            modifier = Modifier
                .size(12.dp)
                .rotate(-45f),
            contentScale = ContentScale.Fit,
        )
    }
}

/** TextField minimalista (placeholder + valor). Sin usar Material3 para evitar dependencias. */
@Composable
private fun BasicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = TextStyle(
            fontSize = 15.sp,
            color = PaquitoColors.TextOnCardStrong,
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(PaquitoColors.BrandPrimary),
        decorationBox = { inner ->
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        color = PaquitoColors.TextInputPlaceholder,
                    )
                }
                inner()
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun ChatInputPreview() {
    PaquitoTheme {
        ChatInput(
            modifier = Modifier.padding(16.dp),
        )
    }
}