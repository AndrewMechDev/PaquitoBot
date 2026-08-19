package pe.tecsup.paquitobot.auth

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import pe.tecsup.paquitobot.data.remote.TokenProvider

/**
 * Guarda la API key mock de `canvas-mock` (`stu_001`, etc. - ver skill
 * `canvas-mock-backend`) cifrada en disco, mismo patron que
 * [SecureTokenStore] para el JWT de Google.
 *
 * Store SEPARADO a proposito: es una credencial de un backend DISTINTO
 * (`canvas-mock`, temporal/demo) con un ciclo de vida propio - no debe
 * mezclarse con la sesion de Google/Canvas real de `paquitobot-rag`.
 */
class CanvasMockKeyStore(context: Context) : TokenProvider {

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

    override suspend fun getToken(): String? = prefs.getString(KEY_API_KEY, null)

    fun saveApiKey(apiKey: String) {
        prefs.edit().putString(KEY_API_KEY, apiKey.trim()).apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    fun hasApiKey(): Boolean = !prefs.getString(KEY_API_KEY, null).isNullOrBlank()

    /** El usuario tocó "Omitir por ahora" en el gate - no volver a pedirlo esta sesión/instalación. */
    fun markSkipped() {
        prefs.edit().putBoolean(KEY_SKIPPED, true).apply()
    }

    fun isSkipped(): Boolean = prefs.getBoolean(KEY_SKIPPED, false)

    private companion object {
        const val PREFS_FILE_NAME = "canvas_mock_session"
        const val KEY_API_KEY = "api_key"
        const val KEY_SKIPPED = "skipped"
    }
}
