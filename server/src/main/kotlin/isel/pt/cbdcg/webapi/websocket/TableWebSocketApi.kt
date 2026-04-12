package isel.pt.cbdcg.webapi.websocket

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import isel.pt.cbdcg.dto.TableWsClientMessage
import kotlinx.serialization.json.Json

fun Route.tableWebSocketApi(hub: WebSocketHub) {

    val json = Json { ignoreUnknownKeys = true }

    webSocket("/ws/tables") {
        try {
            for (frame in incoming) {
                if (frame !is Frame.Text) continue

                val text = frame.readText()
                val message = json.decodeFromString<TableWsClientMessage>(text)

                when (message) {
                    is TableWsClientMessage.SubscribeLobby -> {
                        hub.registerLobby(this)
                    }

                    is TableWsClientMessage.SubscribeTable -> {
                        hub.registerTable(message.tableName, this)
                    }
                }
            }
        } finally {
            hub.unregisterAll(this)
        }
    }
}