package pe.tecsup.paquitobot.data.repository

import pe.tecsup.paquitobot.data.remote.NoOpTokenProvider
import pe.tecsup.paquitobot.data.remote.PaquitoBotApi
import pe.tecsup.paquitobot.data.remote.TokenProvider
import pe.tecsup.paquitobot.data.remote.createPaquitoBotHttpClient
import pe.tecsup.paquitobot.domain.chat.ChatRepository

/**
 * Arma el [ChatRepository] real (Ktor + backend `paquitobot-rag`) sin que
 * el caller (androidApp, y a futuro iosApp) necesite conocer Ktor ni
 * `PaquitoBotApi` directamente - solo la interfaz [ChatRepository] y esta
 * factory. Ktor queda como detalle de implementacion 100% interno a
 * `sharedLogic` (sus dependencias son `implementation`, no `api`).
 *
 * [tokenProvider] default [NoOpTokenProvider] mientras el login del
 * backend esta en standby (ver `TokenProvider.kt`).
 */
fun createDefaultChatRepository(tokenProvider: TokenProvider = NoOpTokenProvider): ChatRepository =
    RemoteChatRepository(
        api = PaquitoBotApi(
            httpClient = createPaquitoBotHttpClient(),
            tokenProvider = tokenProvider,
        ),
    )
