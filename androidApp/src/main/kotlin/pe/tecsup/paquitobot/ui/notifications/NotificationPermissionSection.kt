package pe.tecsup.paquitobot.ui.notifications

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import pe.tecsup.paquitobot.data.notifications.AndroidAcademicNotificationScheduler
import pe.tecsup.paquitobot.ui.theme.PaquitoColors
import pe.tecsup.paquitobot.ui.theme.PaquitoTypography

/** Solo solicita permiso tras una acción explícita; no modifica las categorías guardadas. */
@Composable
fun NotificationPermissionSection(modifier: Modifier = Modifier, onPermissionChanged: () -> Unit = {}) {
    val currentOnPermissionChanged by rememberUpdatedState(onPermissionChanged)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun notificationsAllowed(): Boolean =
        AndroidAcademicNotificationScheduler.notificationsAllowed(context)

    fun permissionMissing(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED

    var allowed by remember { mutableStateOf(notificationsAllowed()) }
    var requested by rememberSaveable { mutableStateOf(false) }
    var settingsUnavailable by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        allowed = notificationsAllowed()
        currentOnPermissionChanged()
    }
    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                allowed = notificationsAllowed()
                currentOnPermissionChanged()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Los avisos disponibles cubren entregas con fecha. Laboratorios y asistencias estarán disponibles más adelante.",
            style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
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
            if (!allowed && !requested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && permissionMissing()) {
                requested = true
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                try {
                    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                            .putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    } else {
                        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.fromParts("package", context.packageName, null))
                    }
                    context.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    settingsUnavailable = true
                }
            }
        }) {
            Text(
                text = if (!allowed && !requested && permissionMissing())
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
