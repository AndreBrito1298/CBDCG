package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.game.Deck
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.repository.GameRepository

object GameRepositoryMem: GameRepository {

    val games = mutableListOf<Game>()

    override fun createGame(players: List<Player>, spectators: List<Spectator>, turnOrder: List<UInt>, startingDeck: Deck<Tile>, itemDeck: Deck<Item>): Game {

        val game = Game(
            id = games.size.toUInt(),
            players = players,
            spectators = spectators,
            board = Board(),
            tileDeck = startingDeck,
            itemDeck = itemDeck,
            turn = Turn(0u, turnOrder, phase = TurnPhase.CONSTRUCTION),
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
