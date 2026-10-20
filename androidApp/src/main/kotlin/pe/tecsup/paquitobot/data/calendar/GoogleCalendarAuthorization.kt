package pe.tecsup.paquitobot.data.calendar

import android.accounts.Account
import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.common.api.Scope
import kotlinx.coroutines.tasks.await

class GoogleCalendarAuthorization(context: Context) {
    private val client = Identity.getAuthorizationClient(context.applicationContext)
    suspend fun authorize(): AuthorizationResult = client.authorize(AuthorizationRequest.builder()
        .setRequestedScopes(SCOPES.map(::Scope)).build()).await()
    fun resolve(data: Intent?): AuthorizationResult = client.getAuthorizationResultFromIntent(data)
    suspend fun clearToken(token: String) { client.clearToken(ClearTokenRequest.builder().setToken(token).build()).await() }
    suspend fun revoke(email: String) { client.revokeAccess(RevokeAccessRequest.builder()
        .setAccount(Account(email, "com.google")).setScopes(SCOPES.map(::Scope)).build()).await() }
    companion object {
        const val CALENDAR_SCOPE = "https://www.googleapis.com/auth/calendar.events.owned"
        val SCOPES = listOf(CALENDAR_SCOPE, "openid", "https://www.googleapis.com/auth/userinfo.email")
    }
}
