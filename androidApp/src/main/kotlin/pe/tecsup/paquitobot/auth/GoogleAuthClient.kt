package pe.tecsup.paquitobot.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import java.security.MessageDigest
import java.util.UUID

/** Fallos del sheet de Google, distintos de un 401 del backend. */
sealed class GoogleSignInFailure(message: String) : Exception(message) {
    data object Cancelled : GoogleSignInFailure("El usuario cancelo el inicio de sesion")
    data object NoAccount : GoogleSignInFailure("No hay una cuenta de Google en este dispositivo")
    class Unknown(cause: Throwable) : GoogleSignInFailure(cause.message ?: "Google Sign-In fallo") {
        init {
            initCause(cause)
        }
    }
}

/**
 * Wrapper de Credential Manager. El boton de [pe.tecsup.paquitobot.ui.auth.AuthGateScreen]
 * usa [GetSignInWithGoogleOption] (Sign in with Google), no One Tap.
 * [GetGoogleIdOption] queda como fallback si Play Services no tiene
 * credencial SiWG.
 *
 * [webClientId] debe coincidir con `GOOGLE_CLIENT_ID` en Render.
 * [signIn] necesita Context de Activity.
 */
class GoogleAuthClient(
    private val context: Context,
    private val webClientId: String,
) {
    private val credentialManager = CredentialManager.create(context)

    /** Devuelve el `id_token` de Google, listo para mandar a `POST /auth/login`. */
    suspend fun signIn(): Result<String> {
        val nonce = hashedNonce()
        val primary = signInWithGoogleButton(nonce)
        val primaryError = primary.exceptionOrNull()
        if (primary.isSuccess || primaryError !is NoCredentialException) {
            return primary.fold(
                onSuccess = { Result.success(it) },
                onFailure = { Result.failure(mapCredentialError(it)) },
            )
        }
        return signInWithGoogleId(nonce).fold(
            onSuccess = { Result.success(it) },
            onFailure = { Result.failure(mapCredentialError(it)) },
        )
    }

    private suspend fun signInWithGoogleButton(nonce: String): Result<String> {
        val option = GetSignInWithGoogleOption.Builder(webClientId)
            .setNonce(nonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        return extractIdToken(request)
    }

    private suspend fun signInWithGoogleId(nonce: String): Result<String> {
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(false)
            .setNonce(nonce)
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(option)
            .build()
        return extractIdToken(request)
    }

    private suspend fun extractIdToken(request: GetCredentialRequest): Result<String> {
        return try {
            val result = credentialManager.getCredential(context, request)
            val credential = GoogleIdTokenCredential.createFrom(result.credential.data)
            Result.success(credential.idToken)
        } catch (exc: GetCredentialException) {
            Result.failure(exc)
        } catch (exc: GoogleIdTokenParsingException) {
            Result.failure(exc)
        }
    }

    private fun mapCredentialError(error: Throwable): GoogleSignInFailure = when (error) {
        is GoogleSignInFailure -> error
        is GetCredentialCancellationException -> GoogleSignInFailure.Cancelled
        is NoCredentialException -> GoogleSignInFailure.NoAccount
        else -> GoogleSignInFailure.Unknown(error)
    }

    private fun hashedNonce(): String {
        val raw = UUID.randomUUID().toString()
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.joinToString("") { byte -> "%02x".format(byte) }
    }
}
