package pe.tecsup.paquitobot.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.clerk.api.Clerk
import com.clerk.api.network.serialization.onSuccess
import pe.tecsup.paquitobot.data.remote.TokenProvider

/**
 * Almacen local cifrado (`EncryptedSharedPreferences`, AES256 con clave en el
 * Android Keystore) para los flags de onboarding y Canvas.
 *
 * Implementa [TokenProvider] delegando en Clerk: [getToken] devuelve el JWT de
 * la sesion activa, o `null` si no hay sesion. El backend debe verificar ese
 * JWT (JWKS de Clerk) en lugar del `access_token` de `POST /auth/login`.
 *
 * `hasCanvasConnected()` es un flag local tras un `POST /auth/canvas/connect`
 * exitoso: el backend no expone todavia un endpoint que diga si el tenant ya
 * tiene Canvas conectado (ver `requerimientos/BACKEND_INTEGRATION.md`).
 */
class SecureTokenStore(context: Context) : TokenProvider {

    private val prefs = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
        )
    }

    /** JWT de la sesion de Clerk; `null` si no hay sesion o no se pudo obtener. */
    override suspend fun getToken(): String? {
        var token: String? = null
        Clerk.auth.getToken().onSuccess { token = it }
        return token
    }

    /** Marca que `POST /auth/canvas/connect` ya se completo con exito en esta sesion. */
    fun markCanvasConnected() {
        prefs.edit().putBoolean(KEY_CANVAS_CONNECTED, true).apply()
    }

    fun hasCanvasConnected(): Boolean = prefs.getBoolean(KEY_CANVAS_CONNECTED, false)

    fun markOnboardingComplete() {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETE, true).apply()
    }

    fun hasCompletedOnboarding(): Boolean = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false)

    private companion object {
        const val PREFS_FILE_NAME = "paquitobot_session"
        const val KEY_CANVAS_CONNECTED = "canvas_connected"
        const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
    }
}
