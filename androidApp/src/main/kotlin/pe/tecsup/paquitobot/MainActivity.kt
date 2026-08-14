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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import kotlinx.coroutines.launch
import pe.tecsup.paquitobot.auth.GoogleAuthClient
import pe.tecsup.paquitobot.auth.SecureTokenStore
import pe.tecsup.paquitobot.session.SessionViewModel
import pe.tecsup.paquitobot.ui.auth.AuthGateScreen
import pe.tecsup.paquitobot.ui.canvas.CanvasConnectScreen
import pe.tecsup.paquitobot.ui.canvas.CanvasConnectViewModel
import pe.tecsup.paquitobot.ui.chat.ChatScreen
import pe.tecsup.paquitobot.ui.chat.ChatViewModel
import pe.tecsup.paquitobot.ui.components.NavTab
import pe.tecsup.paquitobot.ui.home.HomeScreen
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTheme

/**
 * Mientras no haya navegacion real (Compose Navigation), el boton Paquito
 * del Navbar se usa como toggle entre Home (vista principal) y Chat (vista
 * conversacional, con su propia flecha de "volver" para regresar a Home).
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

/**
 * Iteracion 2026-08-13 (flujo de gates): antes de mostrar Home o Chat, la
 * app pasa por dos gates secuenciales evaluados con [SessionViewModel]:
 *
 *   1. Sin sesion de Google -> [AuthGateScreen].
 *   2. Con sesion pero sin Canvas conectado -> [CanvasConnectScreen].
 *   3. Con ambos -> Home/Chat normal, sin volver a pedir nada.
 *
 * Decision del usuario: ambos gates bloquean TODA la app (no solo el
 * chat), y una vez pasados no se vuelven a mostrar durante la sesion.
 */
@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val tokenStore = remember { SecureTokenStore(context.applicationContext) }
    val googleAuthClient = remember {
        GoogleAuthClient(
            context = context,
            webClientId = context.getString(R.string.google_web_client_id),
        )
    }

    val sessionViewModel: SessionViewModel = viewModel(
        factory = remember {
            viewModelFactory {
                initializer { SessionViewModel(tokenStore = tokenStore) }
            }
        },
    )
    val sessionUiState by sessionViewModel.uiState.collectAsStateWithLifecycle()

    when {
        !sessionUiState.isAuthenticated -> AuthGateScreen(
            isSigningIn = sessionUiState.isSigningIn,
            errorMessage = sessionUiState.authError,
            onSignInClick = {
                sessionViewModel.prepareSignIn()
                coroutineScope.launch {
                    googleAuthClient.signIn()
                        .onSuccess { idToken -> sessionViewModel.completeSignIn(idToken) }
                        .onFailure { error -> sessionViewModel.signInFailed(error) }
                }
            },
        )

        !sessionUiState.isCanvasConnected -> {
            val canvasViewModel: CanvasConnectViewModel = viewModel(
                factory = remember {
                    viewModelFactory {
                        initializer { CanvasConnectViewModel(tokenStore = tokenStore) }
                    }
                },
            )
            val canvasUiState by canvasViewModel.uiState.collectAsStateWithLifecycle()
            CanvasConnectScreen(
                uiState = canvasUiState,
                onConnectClick = canvasViewModel::connect,
                onContinueClick = sessionViewModel::markCanvasConnected,
            )
        }

        else -> {
            var currentTab by remember { mutableStateOf(NavTab.Inicio) }
            var rootScreen by remember { mutableStateOf(RootScreen.Home) }

            when (rootScreen) {
                RootScreen.Home -> HomeScreen(
                    currentTab = currentTab,
                    onTabSelected = { currentTab = it },
                    onPaquitoClick = { rootScreen = RootScreen.Chat },
                )
                RootScreen.Chat -> {
                    val chatViewModel: ChatViewModel = viewModel(
                        factory = remember {
                            viewModelFactory {
                                initializer { ChatViewModel(tokenStore = tokenStore) }
                            }
                        },
                    )
                    val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
                    ChatScreen(
                        uiState = chatUiState,
                        onSendMessage = chatViewModel::sendMessage,
                        onBackClick = { rootScreen = RootScreen.Home },
                    )
                }
            }
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
