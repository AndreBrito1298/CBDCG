package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.TURN_DURATION_SECONDS
import isel.pt.cbdcg.domain.game.Deck
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.newDeadline
import isel.pt.cbdcg.repository.GameRepository

object GameRepositoryMem: GameRepository {

    val games = mutableListOf<Game>()

    override suspend fun createGame(players: List<Player>, spectators: List<Spectator>, turnOrder: List<UInt>, startingDeck: Deck<Tile>, itemDeck: Deck<Item>): Game {

        val game = Game(
            id = games.size.toUInt(),
            players = players,
            spectators = spectators,
            board = Board(),
            tileDeck = startingDeck,
            itemDeck = itemDeck,
            turn = Turn(0u, turnOrder, phase = TurnPhase.CONSTRUCTION, newDeadline(TURN_DURATION_SECONDS)),
        )

        games.add(game)
        return game
    }

    override suspend fun getAllGames(): List<Game> {
        return games
    }

    // Generic Operations

    override suspend fun findById(id: UInt): Game? {
        return games.find{ it.id == id }
    }

    override suspend fun save(element: Game) {
        games.removeIf{ it.id == element.id }
        games.add(element)
    }

    override suspend fun deleteById(id: UInt) {

        games.removeIf{ it.id == id }
    }

    override suspend fun clear() {
        games.clear()
    }
}
