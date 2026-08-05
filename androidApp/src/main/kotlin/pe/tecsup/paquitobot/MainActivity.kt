package pe.tecsup.paquitobot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.components.Navbar
import pe.tecsup.paquitobot.ui.onboarding.WelcomeScreen
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
 * Root temporal de la app. Mientras no hay navegación real (ni backend ni
 * NavController de Compose), todas las pantallas se renderizan dentro del
 * Welcome con el Navbar abajo. Esto valida que los componentes extraídos de
 * Figma se ven coherentes.
 */
@Composable
private fun AppRoot() {
    var currentTab by remember { mutableStateOf(NavTab.Inicio) }
    Box(modifier = Modifier.fillMaxSize()) {
        // Pantalla principal (Welcome por ahora; cuando llegue Home B, se
        // hace un switch por currentTab).
        WelcomeScreen(
            onContinueClick = { /* TODO: navegar a Onboarding.Notificaciones */ },
            modifier = Modifier.fillMaxSize(),
        )

        // Navbar anclado abajo.
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 24.dp, start = 16.dp, end = 16.dp),
        ) {
            Navbar(
                currentTab = currentTab,
                onTabSelected = { currentTab = it },
                onPaquitoClick = { /* TODO: navegar a Chat */ },
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppAndroidPreview() {
    PaquitoTheme {
        AppRoot()
    }
}