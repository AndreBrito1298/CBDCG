package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Tile

interface GameRepository: Repository<Game> {

    fun createGame(players: List<Player>, turnOrder: List<UInt>, startingDeck: Map<Tile, UInt>): Game
    fun getAllGames(): List<Game>
}