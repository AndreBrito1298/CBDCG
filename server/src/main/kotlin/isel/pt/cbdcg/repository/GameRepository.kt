package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player

interface GameRepository: Repository<Game> {

    fun createGame(players: List<Player>): Game
    fun getAllGames(): List<Game>
}