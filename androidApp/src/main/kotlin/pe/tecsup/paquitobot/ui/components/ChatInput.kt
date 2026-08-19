package pe.tecsup.paquitobot.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pe.tecsup.paquitobot.R
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoFont
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Input inferior del Chat con boton de enviar, fiel al frame Figma
 * `438:662` (grupo "send" `438:663`).
 *
 *   [_____________input_______________] [Boton enviar]
 *
 * Estilo del input: fondo plano `#F6F6F6`, rounded 20dp, sin borde ni
 * sombra (el frame actual no los tiene - antes tenia border + shadow sutil
 * de una iteracion vieja).
 *
 * Estilo del boton enviar: 47x47, fondo plano `PaquitoColors.BrandPrimary`
 * (`#00C9FB`), rounded 20dp, sin gradiente ni sombra.
 */
@Composable
fun ChatInput(
    modifier: Modifier = Modifier,
    placeholder: String = "Escribe tu mensaje...",
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
                .height(47.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(Color(0xFFF6F6F6))
                .padding(horizontal = 17.dp),
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
    Row(
        modifier = Modifier
            .size(47.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(PaquitoColors.BrandPrimary)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_paquito_send),
            contentDescription = "Enviar mensaje",
            modifier = Modifier.size(width = 17.dp, height = 14.dp),
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
            fontFamily = PaquitoFont.DMSans,
            color = PaquitoColors.TextOnCardStrong,
        ),
        cursorBrush = androidx.compose.ui.graphics.SolidColor(PaquitoColors.BrandPrimary),
        decorationBox = { inner ->
            androidx.compose.foundation.layout.Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontSize = 15.sp,
                        fontFamily = PaquitoFont.DMSans,
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