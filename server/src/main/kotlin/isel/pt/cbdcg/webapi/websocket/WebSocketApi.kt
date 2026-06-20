package isel.pt.cbdcg.webapi.websocket

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import isel.pt.cbdcg.dto.WsClientMessage
import kotlinx.serialization.json.Json

// Registar conforme o tipo de cliente com um enumerado Genérico / Credenciado
// Respostas diferentes para clientes diferentes

fun Route.webSocketApi(hub: WebSocketHub) {

    val json = Json { ignoreUnknownKeys = true }

    webSocket("/ws") {
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue

                val text = frame.readText()
                val message = json.decodeFromString<WsClientMessage>(text)

                when (message) {
                    is WsClientMessage.SubscribeLobby -> {
                        hub.registerLobby(this)
                    }

                    is WsClientMessage.SubscribeTable -> {
                        hub.registerTable(message.tableName, this)
                    }

                    is WsClientMessage.SubscribeGame -> {
                        hub.registerGame(message.gameId, this)
                    }
                }
            }
        } finally {
            hub.unregisterAll(this)
        }
    }
}