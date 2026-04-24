package isel.pt.cbdcg.webapi.websocket

import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.game.Game

interface EventsPublisher {
    suspend fun publishLobbyTables(tables: List<Table>)
    suspend fun publishTableUpdated(table: Table)
    suspend fun publishTableDeleted(table: Table)
    suspend fun publishGameUpdated(game: Game)
}