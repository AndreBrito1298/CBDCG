package isel.pt.cbdcg.webapi.websocket

import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import isel.pt.cbdcg.dto.TableWsClientMessage
import isel.pt.cbdcg.dto.TableWsServerMessage
import isel.pt.cbdcg.dto.toTableOutput
import isel.pt.cbdcg.service.TableService
import kotlinx.serialization.json.Json

fun Route.tableWebSocketApi(hub: WebSocketHub, tableService: TableService) {

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

                        val tables = tableService.getTables()
                        tables.onSuccess { list ->
                            val payload = json.encodeToString<TableWsServerMessage>(
                                TableWsServerMessage.LobbyTables(
                                    tables = list.map{ it.toTableOutput() }
                                )
                            )

                            send(Frame.Text(payload))
                        }
                    }

                    is TableWsClientMessage.SubscribeTable -> {
                        hub.registerTable(message.tableName, this)

                        val tables = tableService.getTables()

                        tables.onSuccess { list ->

                            val table = list.first{ it.name.string == message.tableName }

                            val payload = json.encodeToString<TableWsServerMessage>(
                                TableWsServerMessage.TableInfo(
                                    table = table.toTableOutput()
                                )
                            )

                            send(Frame.Text(payload))
                        }
                    }
                }
            }
        } finally {
            hub.unregisterAll(this)
        }
    }
}