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
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.home.HomeScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Demo de 4 tab del navbar: muestra la pantalla correspondiente segun el tab.
 * Mientras no haya navegacion real, el tab "Paquito btn" se usa como toggle
 * entre Home B (vista principal) y Chat (vista conversacional).
 */
private enum class RootScreen { Home, Chat }

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

@Composable
private fun AppRoot() {
    var currentTab by remember { mutableStateOf(NavTab.Inicio) }
    var rootScreen by remember { mutableStateOf(RootScreen.Home) }

    when (rootScreen) {
        RootScreen.Home -> HomeScreen(
            currentTab = currentTab,
            onTabSelected = { currentTab = it },
            onPaquitoClick = { rootScreen = RootScreen.Chat },
        )
        RootScreen.Chat -> ChatScreen(
            currentTab = currentTab,
            onTabSelected = { currentTab = it; rootScreen = RootScreen.Home },
            onPaquitoClick = { rootScreen = RootScreen.Home },
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    PaquitoTheme {
        AppRoot()
    }
}