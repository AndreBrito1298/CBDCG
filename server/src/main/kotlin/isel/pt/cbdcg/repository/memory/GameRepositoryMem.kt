package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.game.Board
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.repository.GameRepository

object GameRepositoryMem: GameRepository {

    val games = mutableListOf<Game>()

    override fun createGame(players: List<Player>): Game {

        val game = Game(
            id = games.size.toUInt(),
            players = players,
            board = Board(),
            turn = 0u
        )

        games.add(game)
        return game
    }

    override fun getAllGames(): List<Game> {
        return games
    }

    // Generic Operations

    override fun findById(id: UInt): Game? {
        return games.find{ it.id == id }
    }

    override fun save(element: Game) {
        games.removeIf{ it.id == element.id }
        games.add(element)
    }

    override fun deleteById(id: UInt) {
        games.removeIf{ it.id == id }
    }

    override fun clear() {
        games.clear()
    }
}