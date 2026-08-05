package pe.tecsup.paquitobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.home.HomeBScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            PaquitoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = PaquitoColors.Background,
                ) {
                    AppRoot()
                }
            }
        }
    }
}

/**
 * Root temporal. Monta HomeBScreen (la version recomendada del Home del MVP)
 * con todos los componentes extraidos de Figma. Cuando llegue la navegacion
 * real (NavController de Compose), esta raiz se reemplaza por un NavHost.
 */
@Composable
private fun AppRoot() {
    var currentTab by remember { mutableStateOf(NavTab.Inicio) }
    HomeBScreen(
        onDayClick = { /* TODO: navegar a detalle de dia */ },
        onPrimaryAction = { /* TODO: abrir entrega de la tarea */ },
        onSecondaryAction = { /* TODO: programar recordatorio */ },
        currentTab = currentTab,
        onTabSelected = { currentTab = it },
        onPaquitoClick = { /* TODO: navegar a Chat */ },
    )
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    PaquitoTheme {
        AppRoot()
    }
}