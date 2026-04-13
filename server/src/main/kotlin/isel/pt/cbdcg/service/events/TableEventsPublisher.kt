package isel.pt.cbdcg.service.events

import isel.pt.cbdcg.domain.Table

interface TableEventsPublisher {
    suspend fun publishLobbyTables(tables: List<Table>)
    suspend fun publishTableUpdated(table: Table)
    suspend fun publishTableDeleted(table: Table)
}