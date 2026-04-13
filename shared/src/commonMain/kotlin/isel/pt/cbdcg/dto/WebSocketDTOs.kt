package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable
@Serializable
sealed class TableWsServerMessage {

    @Serializable
    data class LobbyTables(
        val tables: List<TableDTO>
    ) : TableWsServerMessage()

    @Serializable
    data class TableInfo(
        val table: TableDTO
    ) : TableWsServerMessage()

    @Serializable
    data class TableDeleted(
        val tableId: Int
    ) : TableWsServerMessage()
}

@Serializable
sealed class TableWsClientMessage {

    @Serializable
    data object SubscribeLobby : TableWsClientMessage()

    @Serializable
    data class SubscribeTable(
        val tableName: String
    ) : TableWsClientMessage()
}