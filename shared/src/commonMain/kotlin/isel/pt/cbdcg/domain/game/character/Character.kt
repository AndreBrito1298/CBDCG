package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.board.Entity

interface Character: Entity {
    val name: String
    val stats: Stats

    fun editStats(newStats: Stats): Character
}