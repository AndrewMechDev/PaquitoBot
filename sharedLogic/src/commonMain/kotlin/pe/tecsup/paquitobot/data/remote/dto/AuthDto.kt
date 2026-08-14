package pe.tecsup.paquitobot.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Payload de `POST /auth/login`. El backend usa `extra="forbid"`, asi que
 * este DTO nunca debe crecer con campos extra.
 */
@Serializable
data class LoginRequestDto(
    @SerialName("id_token")
    val idToken: String,
)

/** Respuesta de `POST /auth/login` (ver `app/schemas/auth.py::LoginResponse`). */
@Serializable
data class LoginResponseDto(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Int,
    val sub: String,
    val email: String? = null,
)
