package pe.tecsup.paquitobot.ui.notifications

import android.Manifest
import android.app.NotificationManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/** Solo solicita permiso tras una acción explícita; no modifica las categorías guardadas. */
@Composable
fun NotificationPermissionSection(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun notificationsAllowed(): Boolean =
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).areNotificationsEnabled()

    var allowed by remember { mutableStateOf(notificationsAllowed()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    var settingsUnavailable by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        allowed = notificationsAllowed()
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) allowed = notificationsAllowed()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Permiso de notificaciones", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextPrimary)
        Text(
            text = if (allowed) "Permiso concedido en este dispositivo."
                else "Android bloquea las notificaciones. Puedes guardar tus preferencias y activar el permiso cuando quieras.",
            style = PaquitoTypography.BodyMedium,
            color = PaquitoColors.TextSecondary,
        )
        Text("El permiso se administra en Android y se aplica al instante, aunque canceles la edición.",
            style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
        OutlinedButton(onClick = {
            if (!allowed && !requested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                requested = true
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                try {
                    context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                        .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                } catch (_: ActivityNotFoundException) {
                    settingsUnavailable = true
                }
            }
        }) {
            Text(
                text = if (!allowed && !requested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                    "Permitir notificaciones" else "Abrir ajustes de notificaciones",
                style = PaquitoTypography.BodyMedium,
            )
        }
        if (settingsUnavailable) {
            Text("Abre los ajustes del dispositivo y busca las notificaciones de PaquitoBot.",
                style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
        }
    }
}
