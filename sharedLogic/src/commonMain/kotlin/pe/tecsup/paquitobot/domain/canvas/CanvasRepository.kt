package pe.tecsup.paquitobot.domain.canvas

/** Puerto de dominio para conectar la cuenta de Canvas del estudiante y sincronizar sus datos. */
interface CanvasRepository {
    /** Envia el token de Canvas al backend para validarlo y persistirlo cifrado. */
    suspend fun connect(canvasToken: String): Result<Unit>

    /**
     * Dispara una sincronizacion manual contra Canvas. Necesaria justo
     * despues de [connect]: el backend tambien sincroniza en background
     * por scheduler, pero sin esto `/query` puede responder sin datos
     * porque el primer sync automatico todavia no corrio.
     */
    suspend fun sync(): Result<Unit>
}
