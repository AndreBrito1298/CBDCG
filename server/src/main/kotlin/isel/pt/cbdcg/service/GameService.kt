package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.Direction
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Tile
import isel.pt.cbdcg.domain.verifyToken
import isel.pt.cbdcg.error.GameError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.GameRepository
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.webapi.websocket.EventsPublisher

class GameService(
    private val gameRepo: GameRepository,
    private val tableRepo: TableRepository,
    private val userRepo: UserRepository,
    private val events: EventsPublisher,
) {

    suspend fun createGame(tableId: UInt, userId: UInt, token: String): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(table.owner.id != userId)
            throw TableError.OwnerOnly()

        if(table.participants.any{ it.role == Role.PLAYER })
            throw GameError.EveryPlayerReady()

        val startingDeck = mutableMapOf(
            Tile(Direction.entries) to 10u,
            Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)) to 18u,
            Tile(listOf(Direction.EAST, Direction.NORTH)) to 31u,
            Tile(listOf(Direction.NORTH, Direction.SOUTH)) to 31u,
        )

        val players = table.participants.filter{ it.role == Role.READY }
            .map{ player ->

                val hand = mutableMapOf<UInt, Tile>()

                repeat(3){ idx ->
                    val available = startingDeck.filterValues { it > 0u }.keys.toList()
                    val drawnTile = available.random()

                    hand[idx.toUInt()] = drawnTile
                    startingDeck[drawnTile] = startingDeck[drawnTile]!! - 1u
                }

                Player(player.user.id, hand)
            }

        if(players.size < 2)
            throw GameError.MinimumPlayersNeeded()

        val turnOrder = players.map{ it.user }

        val game = gameRepo.createGame(players, turnOrder, startingDeck)
        events.publishGameStarted(table, game)

        game
    }

    suspend fun placeTile(userId: UInt, gameId: UInt, token: String, tile: Tile, idx: UInt, pos: BoardPosition): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = game.placeTile(player, pos, tile, idx)
            .resolveState()

        gameRepo.save(newGame)

        events.publishGameUpdated(newGame)
        newGame

    }

    fun rotateTile(userId: UInt, gameId: UInt, token: String, idx: UInt, right: Boolean): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())


        val newPlayers = game.players.map{ player ->
            if(player.user == userId) {

                val newHand = player.hand.map{ (index, tile) ->
                    if(idx == index) index to tile.rotate(right)
                    else index to tile
                }.toMap()

                player.copy(hand = newHand)
            } else player
        }

        val newGame = game.copy(players = newPlayers)
        gameRepo.save(newGame)

        newGame
    }
}