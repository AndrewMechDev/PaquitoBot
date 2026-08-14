package pe.tecsup.paquitobot.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import pe.tecsup.paquitobot.data.remote.TokenProvider

/**
 * Guarda el JWT del backend (`access_token` de `POST /auth/login`) cifrado
 * en disco con `EncryptedSharedPreferences` (AES256, clave en el Android
 * Keystore) - nunca en `SharedPreferences` planas.
 *
 * Implementa [TokenProvider]: es la pieza real que reemplaza a
 * `NoOpTokenProvider` ahora que el login existe (ver
 * `requerimientos/BACKEND_INTEGRATION.md`, seccion "Como esta resuelto del
 * lado del cliente"). [getToken] devuelve `null` si no hay sesion O si el
 * token ya vencio - el caller (`PaquitoBotApi.query`) lo trata igual que
 * "no autenticado" en ambos casos, no hace falta distinguir aca.
 *
 * Iteracion 2026-08-13 (gate de Canvas): tambien guarda un flag local
 * `hasCanvasConnected()` tras un `POST /auth/canvas/connect` exitoso, para
 * que la app no vuelva a pedir el token de Canvas en cada sesion. El
 * backend no expone (todavia) un endpoint que diga "este tenant ya tiene
 * Canvas conectado" - es una limitacion conocida: si se borra el flag
 * local (o se reinstala la app) sin que el usuario haya perdido la sesion
 * de Google, se le va a volver a pedir el token aunque el backend ya lo
 * tenga guardado. Documentado en `requerimientos/BACKEND_INTEGRATION.md`.
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

    override suspend fun getToken(): String? {
        if (!hasValidSession()) return null
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /** Persiste la sesion tras un login exitoso. [expiresInSeconds] viene de `LoginResponse.expires_in`. */
    fun saveSession(accessToken: String, expiresInSeconds: Int) {
        val expiresAtEpochMillis = System.currentTimeMillis() + expiresInSeconds * 1000L
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putLong(KEY_EXPIRES_AT, expiresAtEpochMillis)
            .apply()
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }

    /** `true` solo si hay un token guardado Y todavia no vencio. */
    fun hasValidSession(): Boolean {
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return prefs.contains(KEY_ACCESS_TOKEN) && System.currentTimeMillis() < expiresAt
    }

    /** Marca que `POST /auth/canvas/connect` ya se completo con exito en esta sesion. */
    fun markCanvasConnected() {
        prefs.edit().putBoolean(KEY_CANVAS_CONNECTED, true).apply()
    }

    fun hasCanvasConnected(): Boolean = prefs.getBoolean(KEY_CANVAS_CONNECTED, false)

    private companion object {
        const val PREFS_FILE_NAME = "paquitobot_session"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_EXPIRES_AT = "expires_at_epoch_millis"
        const val KEY_CANVAS_CONNECTED = "canvas_connected"
    }
}
