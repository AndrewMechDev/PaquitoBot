package pe.tecsup.paquitobot.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import pe.tecsup.paquitobot.ui.components.ProfileIdentityCard
import pe.tecsup.paquitobot.ui.procedures.ProcedureEntryCard
import pe.tecsup.paquitobot.ui.theme.*

/** Identidad de Clerk; los datos del catálogo académico no identifican al usuario. */
@Composable
fun ProfileTabContent(
    name: String,
    email: String,
    isSigningOut: Boolean,
    signOutError: String?,
    onProceduresClick: () -> Unit,
    onEnrollmentClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onAcademicClick: () -> Unit = {},
    onCoursesClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var confirmSignOut by rememberSaveable { mutableStateOf(false) }
    Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Text("Mi perfil", style = PaquitoTypography.DisplayLarge, color = PaquitoColors.TextHomeStrong)
        ProfileIdentityCard(name, email)
        Text("Servicios al estudiante", style = PaquitoTypography.HeadlineSmall, color = PaquitoColors.TextHomeStrong)
        ProcedureEntryCard(onClick = onAcademicClick, title = "Notas y asistencias", subtitle = "Entiende tus alertas y revisa cómo vas")
        ProcedureEntryCard(onClick = onCoursesClick, title = "Mis cursos", subtitle = "Evaluaciones, faltas y próximas entregas")
        ProcedureEntryCard(onClick = onProceduresClick)
        ProcedureEntryCard(onClick = onEnrollmentClick, title = "Guía de matrícula", subtitle = "Requisitos y pasos para tu matrícula")
        Text("Los cursos, notas, horarios y tareas son datos de demostración. No corresponden a tu cuenta institucional.",
            style = PaquitoTypography.BodyMedium, color = PaquitoColors.TextOnCardMuted)
        OutlinedButton(onClick = { confirmSignOut = true }, enabled = !isSigningOut,
            modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PaquitoColors.StateDanger)) {
            Text(if (isSigningOut) "Cerrando sesión…" else "Cerrar sesión", style = PaquitoTypography.BodyMedium)
        }
        signOutError?.let { Text(it, style = PaquitoTypography.BodyMedium, color = PaquitoColors.StateDanger) }
    }
    if (confirmSignOut) AlertDialog(
        onDismissRequest = { confirmSignOut = false },
        containerColor = PaquitoColors.Background,
        title = { Text("¿Cerrar sesión?", style = PaquitoTypography.HeadlineSmall) },
        text = { Text("Podrás volver a ingresar con tu cuenta institucional.", style = PaquitoTypography.BodyMedium) },
        confirmButton = { TextButton(onClick = { confirmSignOut = false; onSignOutClick() }) {
            Text("Cerrar sesión", style = PaquitoTypography.BodyMedium, color = PaquitoColors.StateDanger)
        } },
        dismissButton = { TextButton(onClick = { confirmSignOut = false }) {
            Text("Volver", style = PaquitoTypography.BodyMedium, color = PaquitoColors.StateInfoStrong)
        } },
    )
}

@Preview(showBackground = true, widthDp = 360, heightDp = 1000)
@Composable
private fun ProfilePreview() {
    PaquitoTheme { ProfileTabContent("Estudiante de ejemplo", "estudiante@tecsup.edu.pe", false, null, {}, {}, {}) }
}
