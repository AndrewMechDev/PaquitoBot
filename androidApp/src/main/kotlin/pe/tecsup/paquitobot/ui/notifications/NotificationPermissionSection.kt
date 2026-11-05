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
import androidx.compose.runtime.LaunchedEffect
import androidx.work.WorkManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.ExistingWorkPolicy
import androidx.work.WorkInfo
import androidx.work.workDataOf
import androidx.work.OutOfQuotaPolicy
import pe.tecsup.paquitobot.auth.authorizedAcademicUserId
import pe.tecsup.paquitobot.data.notifications.AcademicNotificationPublisher
import pe.tecsup.paquitobot.data.notifications.NotificationTestWorker
import pe.tecsup.paquitobot.ui.components.PaquitoPrimaryButton
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
fun NotificationPermissionSection(modifier: Modifier = Modifier, onPermissionChanged: () -> Unit = {}, compactDemo: Boolean = false) {
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
    var pendingDemo by rememberSaveable { mutableStateOf(false) }
    val workManager = remember(context) { WorkManager.getInstance(context.applicationContext) }
    var testError by remember { mutableStateOf(false) }
    fun startDemo() {
        val owner = authorizedAcademicUserId()?.let(AcademicNotificationPublisher::ownerKey) ?: return
        try {
            val request = OneTimeWorkRequestBuilder<NotificationTestWorker>()
                .apply {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    }
                }
                .addTag("owner:$owner").addTag("requested:${System.currentTimeMillis()}")
                .setInputData(workDataOf("owner" to owner, "demo" to "academic_risk")).build()
            workManager.enqueueUniqueWork(NotificationTestWorker.WORK_NAME, ExistingWorkPolicy.REPLACE, request)
            testError = false
        } catch (_: IllegalStateException) { testError = true }
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        allowed = notificationsAllowed()
        currentOnPermissionChanged()
        if (allowed && pendingDemo) startDemo()
        pendingDemo = false
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

    var testStatus by remember { mutableStateOf<WorkInfo?>(null) }
    LaunchedEffect(workManager) {
        workManager.getWorkInfosForUniqueWorkFlow(NotificationTestWorker.WORK_NAME).collect { work ->
            val currentOwner = authorizedAcademicUserId()?.let(AcademicNotificationPublisher::ownerKey)
            testStatus = work.filter { currentOwner != null && "owner:$currentOwner" in it.tags }.maxByOrNull { info -> info.tags.firstOrNull { it.startsWith("requested:") }?.substringAfter(":")?.toLongOrNull() ?: 0L }
        }
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!compactDemo) {
        Text("Entregas, laboratorios calificados e inasistencias registradas. Usamos datos de demostración. Android puede aplazar los avisos por ahorro de batería.",
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
        }
        if (authorizedAcademicUserId() != null) {
            val running = testStatus?.state?.isFinished == false
            PaquitoPrimaryButton(text = if (compactDemo && running) "Detener demo" else "Iniciar demo de todos los avisos", enabled = compactDemo || (allowed && !running), onClick = {
                if (compactDemo && running) {
                    workManager.cancelUniqueWork(NotificationTestWorker.WORK_NAME)
                } else if (allowed) {
                    startDemo()
                } else if (permissionMissing()) {
                    pendingDemo = true
                    requested = true
                    launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    try {
                        context.startActivity(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName))
                    } catch (_: ActivityNotFoundException) { settingsUnavailable = true }
                }
            })
            if (!compactDemo && running) {
                OutlinedButton(onClick = { workManager.cancelUniqueWork(NotificationTestWorker.WORK_NAME) }) {
                    Text("Detener demo", style = PaquitoTypography.BodyMedium)
                }
            }
            if (!compactDemo || testError || testStatus?.state == WorkInfo.State.FAILED) Text(when {
                testError -> "No se pudo programar la prueba. Vuelve a intentarlo."
                testStatus?.outputData?.getString("status") == "no_risks" -> "No hay riesgos de nota ni asistencia en tus cursos actuales."
                testStatus?.state == WorkInfo.State.SUCCEEDED -> "Demo completada: ${testStatus?.outputData?.getInt("sent", 0)} avisos enviados. Revisa la barra de notificaciones."
                testStatus?.state == WorkInfo.State.FAILED && testStatus?.outputData?.getString("status") == "session_changed" -> "La demo se detuvo porque cambió tu sesión. Vuelve a iniciar sesión e inicia la demo otra vez."
                testStatus?.state == WorkInfo.State.FAILED && testStatus?.outputData?.getString("status") == "session_unavailable" -> "No se pudo preparar tu sesión para la demo. Abre la app e inténtalo otra vez."
                testStatus?.state == WorkInfo.State.FAILED -> "No se envió el aviso. Revisa el permiso y tu sesión e inténtalo de nuevo."
                testStatus?.state == WorkInfo.State.CANCELLED -> "Demo detenida. Puedes iniciarla otra vez."
                testStatus?.state == WorkInfo.State.RUNNING -> "Enviados ${testStatus?.progress?.getInt("sent", 0)} de ${testStatus?.progress?.getInt("total", 0)}. Primer aviso en 15 segundos; los siguientes cada 5 segundos. Puedes dejar la app en segundo plano."
                testStatus?.state?.isFinished == false -> "Demo en cola. Android está preparando el inicio de la secuencia."
                else -> "Demo: empieza a los 15 segundos y muestra un aviso cada 5 segundos: notas, faltas, entregas, laboratorios y resumen. Hasta 12 avisos de tus cursos."
            }, style = PaquitoTypography.BodySmall, color = PaquitoColors.TextSecondary)
        }
        if (settingsUnavailable) {
            Text("Abre los ajustes del dispositivo y busca las notificaciones de PaquitoBot.",
                style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextSecondary)
        }
    }
}
