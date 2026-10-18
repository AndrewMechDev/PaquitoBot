package pe.tecsup.paquitobot.auth

import com.clerk.api.Clerk
import com.clerk.api.network.model.verification.Verification
import com.clerk.api.user.User

/** Misma autorización institucional para las pantallas y los avisos de fondo. */
internal fun User.hasVerifiedTecsupEmail(): Boolean {
    val primary = emailAddresses.orEmpty().firstOrNull { it.id == primaryEmailAddressId } ?: return false
    val email = primary.emailAddress
    val at = email.indexOf('@')
    return primary.verification?.status == Verification.Status.VERIFIED &&
        at > 0 && at == email.lastIndexOf('@') &&
        email.substring(at + 1).equals("tecsup.edu.pe", ignoreCase = true)
}

internal fun authorizedAcademicUserId(): String? {
    if (!Clerk.isInitialized.value || Clerk.activeSession == null) return null
    return Clerk.activeUser?.takeIf { it.hasVerifiedTecsupEmail() }?.id
}
