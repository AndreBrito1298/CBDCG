package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable
@Serializable
sealed class TableWsServerMessage {

    @Serializable
    data class LobbyTables(
        val tables: List<TableOutput>
    ) : TableWsServerMessage()

    @Serializable
    data class TableInfo(
        val table: TableOutput
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