package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable
@Serializable
sealed class WsServerMessage {

    @Serializable
    data class LobbyTables(
        val tables: List<TableDTO>
    ) : WsServerMessage()

    @Serializable
    data class TableInfo(
        val table: TableDTO
    ) : WsServerMessage()

    @Serializable
    data class TableDeleted(
        val tableId: Int
    ) : WsServerMessage()

    @Serializable
    data class GameInfo(
        val game: GameDTO
    ) : WsServerMessage()
}

@Serializable
sealed class WsClientMessage {

    @Serializable
    data object SubscribeLobby : WsClientMessage()

    @Serializable
    data class SubscribeTable(
        val tableName: String
    ) : WsClientMessage()

    @Serializable
    data class SubscribeGame(
        val gameId: Int
    ) : WsClientMessage()
}