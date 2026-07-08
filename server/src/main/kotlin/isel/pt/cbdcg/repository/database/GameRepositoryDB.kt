package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.game.Deck
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.toBattleDTO
import isel.pt.cbdcg.domain.game.toTurnDTO
import isel.pt.cbdcg.dto.TurnDTO
import isel.pt.cbdcg.repository.GameRepository
import isel.pt.cbdcg.repository.database.Tables.Game.GamePlayers
import isel.pt.cbdcg.repository.database.Tables.Game.GamePlayersDao
import isel.pt.cbdcg.repository.database.Tables.Game.GameSpectators
import isel.pt.cbdcg.repository.database.Tables.Game.GameSpectatorsDao
import isel.pt.cbdcg.repository.database.Tables.Game.Games
import isel.pt.cbdcg.repository.database.Tables.Game.GamesDao
import isel.pt.cbdcg.repository.database.Tables.Game.deleteBoardForGame
import isel.pt.cbdcg.repository.database.Tables.Game.itemDeckToDb
import isel.pt.cbdcg.repository.database.Tables.Game.saveBoard
import isel.pt.cbdcg.repository.database.Tables.Game.tileDeckToDb
import isel.pt.cbdcg.repository.database.Tables.Game.toGame
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.jetbrains.exposed.v1.jdbc.update

object GameRepositoryDB: GameRepository{
    override suspend fun createGame(
        players: List<Player>,
        spectators: List<Spectator>,
        turnOrder: List<UInt>,
        startingDeck: Deck<Tile>,
        itemDeck: Deck<Item>
    ): Game =
        suspendTransaction {
            val game = GamesDao.new {
                //gameTurn = 0
               // currentTurnPhase = TurnPhase.CONSTRUCTION
                version = 0u
                turn = Turn(0u, turnOrder, TurnPhase.CONSTRUCTION, 0).toTurnDTO()
                itemsDeck = itemDeck.itemDeckToDb()
                tileDeck = startingDeck.tileDeckToDb()
              //  currentPlayer = players.minByOrNull { turnOrder.indexOf(it.user.id) }!!.user.id.toInt()
                battle = null
              //  playerTurnQueue = turnOrder.map { it.toInt() }.toTypedArray()   // NEW
            }

            players.forEach { player ->
                GamePlayersDao.new {
                    gameId = game.id.value
                    userId = player.user.id.toInt()

                    playerHands = player.hand
                        .toSortedMap()
                        .values
                        .map { it.toCardDTO() }
                        .toTypedArray()
                    currentCharacter = player.currentCharacter
                }
            }

            spectators.forEach { spectator ->
                GameSpectatorsDao.new {
                    gameId = game.id.value
                    userId = spectator.user.id.toInt()
                }
            }

            saveBoard(game.id.value, Board())

            game.toGame()
        }

    override suspend fun getAllGames(): List<Game> {
       return suspendTransaction {
           GamesDao.all().map { it.toGame() }
        }
    }

    override suspend fun findById(id: UInt): Game? {
        return suspendTransaction {
            GamesDao.find { Games.id eq id.toInt() }.singleOrNull()?.toGame()
        }
    }override suspend fun save(element: Game) {
        return suspendTransaction {
            val game = GamesDao.findById(element.id.toInt()) ?: throw Exception("Game not found")

            element.players.forEach { player ->
                GamePlayers.update({ (GamePlayers.userId eq player.user.id.toInt()) and (GamePlayers.gameId eq game.id.value) }) { gamePlayer ->
                    gamePlayer[playerHands] = player.hand.toSortedMap().values.map { it.toCardDTO() }.toTypedArray()
                    gamePlayer[currentCharacter] = player.currentCharacter
                }
            }
            game.version = 1u
            //game.version = element.version
            game.turn = element.turn.toTurnDTO()
           // game.playerTurnQueue = element.turn.playerTurn.map { it.toInt() }.toTypedArray()  // persist the queue itself
            game.itemsDeck = element.itemDeck.itemDeckToDb()
            game.tileDeck = element.tileDeck.tileDeckToDb()
          //  game.gameTurn = element.turn.gameTurn.toInt()
          //  game.currentTurnPhase = element.turn.phase
            game.battle = element.battle?.toBattleDTO()

            saveBoard(game.id.value, element.board)
        }
    }



    override suspend fun deleteById(id: UInt) {
        return suspendTransaction {
            deleteGameChildren(id.toInt())
            GamesDao.findById(id.toInt())?.delete()
        }
    }

    override suspend fun clear() {
        suspendTransaction {
            GamesDao.all().forEach {
                deleteGameChildren(it.id.value)
                it.delete()
            }
        }
    }
    private fun deleteGameChildren(gameId: Int) {
        GameSpectatorsDao.find { GameSpectators.gameId eq gameId }.forEach { it.delete() }
        GamePlayersDao.find { GamePlayers.gameId eq gameId }.forEach { it.delete() }
        deleteBoardForGame(gameId)
    }
}
