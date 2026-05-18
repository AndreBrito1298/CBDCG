package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.applyBoardTileEffect
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.Effect
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.rotate
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.domain.game.draw
import isel.pt.cbdcg.domain.game.nextPhase
import isel.pt.cbdcg.domain.game.nextTurn
import isel.pt.cbdcg.domain.game.placeOnBoard
import isel.pt.cbdcg.error.GameError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.GameRepository
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.service.SimpleCrypto.verifyToken
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
        user.auth.verifyToken(token)

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(table.owner.id != userId)
            throw TableError.OwnerOnly()

        if(table.participants.any{ it.role == Role.PLAYER })
            throw GameError.EveryPlayerReady()

        val startingDeck = mutableMapOf(
            Tile(Direction.entries) to 12u,
            Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)) to 22u,
            Tile(listOf(Direction.EAST, Direction.NORTH)) to 28u,
            Tile(listOf(Direction.NORTH, Direction.SOUTH)) to 28u,
        )

        val characters = PlayableCharacterCatalog.playableCharacters.shuffled()

        val players = table.participants.filter{ it.role == Role.READY }
            .mapIndexed{ playerIdx, player ->

                val hand = mutableMapOf<UInt, Card>()

                repeat(3){ idx ->

                    val drawnTile = startingDeck.draw()

                    hand[idx.toUInt()] = TileCard(drawnTile)
                    startingDeck[drawnTile] = startingDeck[drawnTile]!! - 1u
                }

                hand[3u] = CharacterCard(characters[playerIdx * 2])
                hand[4u] = CharacterCard(characters[playerIdx * 2 + 1])

                Player(player.user, hand, null)
            }

        val spectators = table.participants.filter{ it.role == Role.SPECTATOR }
            .map{ spectator -> Spectator(spectator.user) }

        if(players.size < 2)
            throw GameError.MinimumPlayersNeeded()

        val turnOrder = players.map{ it.user.id }

        val game = gameRepo.createGame(players, spectators, turnOrder, startingDeck)
        events.publishGameStarted(table, game)

        game
    }
    suspend fun placeOnBoard(userId: UInt, gameId: UInt, token: String, card: Card, idx: UInt, pos: BoardPosition): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        user.auth.verifyToken(token)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = if(game.turn.gameTurn == 0u){
            game.placeOnBoard(player, pos, card, idx).nextTurn()
        } else {
            game.placeOnBoard(player, pos, card, idx)
        }

        gameRepo.save(newGame)

        events.publishGameUpdated(newGame)

        newGame

    }

    fun rotateTile(userId: UInt, gameId: UInt, token: String, idx: UInt, right: Boolean): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        user.auth.verifyToken(token)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())


        val newPlayers = game.players.map{ player ->
            if(player.user.id == userId) {

                val newHand = player.hand.map{ (index, card) ->
                    if(index == idx) {
                        when(card){
                            is TileCard -> index to card.copy(tile = card.tile.rotate(right))
                            else -> index to card
                        }
                    } else index to card
                }.toMap()

                player.copy(hand = newHand)
            } else {
                player
            }
        }

        val newGame = game.copy(players = newPlayers)
        gameRepo.save(newGame)

        newGame
    }
    suspend fun nextPhase(userId: UInt, gameId: UInt, token: String): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        user.auth.verifyToken(token)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game.nextPhase()

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)

        newGame
    }

    suspend fun applyBoardTileEffect(
        userId: UInt,
        gameId: UInt,
        token: String,
        effect: Effect<BoardTile>,
        origin: BoardTile,
        vararg targets: BoardTile,
    ): Result<Game> = runCatching {
        val user = userRepo.findById(userId) ?: throw UserError.IdNotFound()
        user.auth.verifyToken(token)

        val game = gameRepo.findById(gameId) ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game.applyBoardTileEffect(effect, origin, *targets)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
}