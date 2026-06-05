package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.addToGame
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.updateStatModifiers
import isel.pt.cbdcg.domain.game.applyRandomSpecialEffects
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.Entity
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.gameUpdateByName
// import isel.pt.cbdcg.domain.game.board.applyBoardTileUpdater
import isel.pt.cbdcg.domain.game.board.rotate
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.domain.game.draw
import isel.pt.cbdcg.domain.game.drawItem
import isel.pt.cbdcg.domain.game.leaveGame
import isel.pt.cbdcg.domain.game.moveCharacter
import isel.pt.cbdcg.domain.game.nextPhase
import isel.pt.cbdcg.domain.game.placeOnBoard
import isel.pt.cbdcg.domain.game.resolveTurnZero
import isel.pt.cbdcg.domain.game.unequip
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

    suspend fun createGame(tableId: UInt, userId: UInt): Result<Game> = runCatching {
        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(table.owner.id != userId)
            throw TableError.OwnerOnly()

        if(table.participants.any{ it.role == Role.PLAYER })
            throw TableError.EveryPlayerReady()


        val startingDeck = mutableMapOf(
            Tile(Direction.entries) to 13u,
            Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)) to 24u,
            Tile(listOf(Direction.EAST, Direction.NORTH)) to 31u,
            Tile(listOf(Direction.NORTH, Direction.SOUTH)) to 31u,
        ).applyRandomSpecialEffects().toMutableMap()

        val characters = PlayableCharacterCatalog.playableCharacters.shuffled()
        val allItems = ItemCatalog.items.associateWith { 1u }

        // Para testar o "fim de jogo" e tudo mais, estamos a permitir que os KEY items possam ser obtidos no início.

        // val itemDeck = allItems.filter{ it.key.grade != Grade.KEY }.toMutableMap()
        val itemDeck = allItems.toMutableMap()

        val players = table.participants.filter{ it.role == Role.READY }
            .mapIndexed{ playerIdx, player ->

                val hand = mutableMapOf<UInt, Card>()

                repeat(3){ idx ->

                    val drawnTile = startingDeck.draw()
                    val drawItem = itemDeck.draw()

                    hand[idx.toUInt()] = TileCard(drawnTile)
                    hand[5u + idx.toUInt()] = ItemCard(drawItem)

                    startingDeck[drawnTile] = startingDeck[drawnTile]!! - 1u
                    itemDeck[drawItem] = 0u
                }

                hand[3u] = CharacterCard(characters[playerIdx * 2])
                hand[4u] = CharacterCard(characters[playerIdx * 2 + 1])

                Player(player.user, hand.toList().sortedBy { it.first }.toMap(), null)
            }

        val spectators = table.participants.filter{ it.role == Role.SPECTATOR }
            .map{ spectator -> Spectator(spectator.user) }

        if(players.size < 2)
            throw TableError.MinimumPlayersNeeded()

        val turnOrder = players.map{ it.user.id }

        val game = gameRepo.createGame(players, spectators, turnOrder, startingDeck, itemDeck)
        user.addToGame(game.id, userRepo)
        players.forEach { player ->
            player.user.addToGame(game.id, userRepo)
        }

        events.publishGameStarted(table, game)

        game
    }

    fun rotateTile(userId: UInt, gameId: UInt, token: String, idx: UInt, right: Boolean): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

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
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game.nextPhase()

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)

        newGame
    }

    suspend fun leaveGame(userId: UInt, gameId: UInt, token: String): Result<Unit> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = game.leaveGame(player)
        events.publishGameUpdated(newGame)

    }

    // Effects to be implemented

    suspend fun placeOnBoard(userId: UInt, gameId: UInt, token: String, card: Card, idx: UInt, pos: BoardPosition): Result<Game> = runCatching {
        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = if(game.turn.gameTurn == 0u){
            game.placeOnBoard(player, pos, card, idx).resolveTurnZero()
        } else {
            game.placeOnBoard(player, pos, card, idx)
        }

        gameRepo.save(newGame)

        events.publishGameUpdated(newGame)

        newGame

    }

    suspend fun moveCharacter(userId: UInt, gameId: UInt, token: String, from: BoardTile, to: BoardTile): Result<Game> = runCatching{

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        if(game.turn.phase != TurnPhase.MOVEMENT)
            throw GameError.CharacterMovementRestriction()

        val newGame = game.moveCharacter(from, to)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
    suspend fun unequip(userId: UInt, gameId: UInt, token: String, character: Character, index: Int): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = game.unequip(player, character, index)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)

        newGame
    }

    suspend fun applyGameUpdater(
        userId: UInt,
        gameId: UInt,
        token: String,
        updaterName: String,
        origin: Entity,
        targets: List<Entity>,
    ): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game.gameUpdateByName(updaterName, origin, targets)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }

    suspend fun drawItem(userId: UInt, gameId: UInt, token: String, trigger: BoardTile): Result<Game> = runCatching{

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = game.drawItem(player, trigger)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }

    suspend fun updateStatModifiers(userId: UInt, gameId: UInt, token: String, origin: BoardTile): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame = game.updateStatModifiers(player, origin)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
}