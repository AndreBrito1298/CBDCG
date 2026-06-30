package isel.pt.cbdcg.service

import isel.pt.cbdcg.INITIAL_CHARACTER_CARDS
import isel.pt.cbdcg.INITIAL_ITEM_CARDS
import isel.pt.cbdcg.INITIAL_TILE_CARDS
import isel.pt.cbdcg.MIN_PLAYERS_TO_START
import isel.pt.cbdcg.NUM_2_WAY_TILES
import isel.pt.cbdcg.NUM_3_WAY_TILES
import isel.pt.cbdcg.NUM_4_WAY_TILES
import isel.pt.cbdcg.NUM_COPIES_CHARACTER
import isel.pt.cbdcg.NUM_COPIES_ITEM
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.addToGame
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.updateStatModifiers
import isel.pt.cbdcg.domain.game.applyRandomSpecialEffects
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.Entity
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.addActionToPending
import isel.pt.cbdcg.domain.game.battle
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.gameUpdateByName
import isel.pt.cbdcg.domain.game.board.tile.rotate
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.domain.game.deleteBattle
import isel.pt.cbdcg.domain.game.draw
import isel.pt.cbdcg.domain.game.drawItem
import isel.pt.cbdcg.domain.game.joinBattle
import isel.pt.cbdcg.domain.game.leaveBattle
import isel.pt.cbdcg.domain.game.leaveGame
import isel.pt.cbdcg.domain.game.nextPhase
import isel.pt.cbdcg.domain.game.placeOnBoard
import isel.pt.cbdcg.domain.game.removeActionFromPending
import isel.pt.cbdcg.domain.game.resolvePending
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


        val availableTiles = mutableMapOf(
            Tile(Direction.entries) to NUM_4_WAY_TILES,
            Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)) to NUM_3_WAY_TILES,
            Tile(listOf(Direction.EAST, Direction.NORTH)) to NUM_2_WAY_TILES,
            Tile(listOf(Direction.NORTH, Direction.SOUTH)) to NUM_2_WAY_TILES,
        ).applyRandomSpecialEffects().toMutableMap()

        val availableCharacters = PlayableCharacterCatalog.basicCharacters
            .shuffled()
            .associateWith { NUM_COPIES_CHARACTER }
            .toMutableMap()

        val commonItems = ItemCatalog.commonItems
            .associateWith { NUM_COPIES_ITEM }
            .toMutableMap()
        val specialItems = ItemCatalog.specialItems
            .associateWith { NUM_COPIES_ITEM }

        val players = table.participants.filter{ it.role == Role.READY }
            .map{ player ->

                val hand = mutableMapOf<UInt, Card>()

                repeat(INITIAL_TILE_CARDS){ idx ->
                    val drawnTile = availableTiles.draw()
                    hand[idx.toUInt()] = TileCard(drawnTile)
                    val remaining = availableTiles[drawnTile] ?: 0u
                    availableTiles[drawnTile] = (remaining - 1u).coerceAtLeast(0u)
                }

                val lastTileIdx = hand.size.toUInt()

                repeat(INITIAL_ITEM_CARDS){ idx ->
                    val drawnItem = commonItems.draw()
                    hand[idx.toUInt() + lastTileIdx] = ItemCard(drawnItem)
                    val remaining = commonItems[drawnItem] ?: 0u
                    commonItems[drawnItem] = (remaining - 1u).coerceAtLeast(0u)
                }

                val lastItemIdx = hand.size.toUInt()

                repeat(INITIAL_CHARACTER_CARDS){ idx ->
                    val drawnCharacter = availableCharacters.draw()
                    hand[idx.toUInt() + lastItemIdx] = CharacterCard(drawnCharacter)
                    val remaining = availableCharacters[drawnCharacter] ?: 0u
                    availableCharacters[drawnCharacter] = (remaining - 1u).coerceAtLeast(0u)
                }

                Player(player.user, hand.toList().sortedBy { it.first }.toMap(), null)
            }

        val spectators = table.participants.filter{ it.role == Role.SPECTATOR }
            .map{ spectator -> Spectator(spectator.user) }

        if(players.size < MIN_PLAYERS_TO_START)
            throw TableError.MinimumPlayersNeeded()

        val turnOrder = players.map{ it.user.id }

        val game = gameRepo.createGame(players, spectators, turnOrder, availableTiles, commonItems + specialItems)
        user.addToGame(game.id, userRepo)
        players.forEach { player ->
            player.user.addToGame(game.id, userRepo)
        }

        events.publishGameStarted(table, game)

        tableRepo.deleteById(table.id)
        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)

        game
    }

    suspend fun rotateTile(userId: UInt, gameId: UInt, token: String, idx: UInt, right: Boolean): Result<Game> = runCatching {

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
    suspend fun battle(userId: UInt, gameId: UInt, token: String, attacker: Character, defender: Character): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game.battle(attacker, defender).resolvePending()

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
    suspend fun participateInBattle(userId: UInt, gameId: UInt, token: String, character: Character, accept: Boolean): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val player = game.players.find{ it.user.id == user.id }
            ?: throw GameError.PlayerNotFound(user.email.string, game.id.toInt())

        val newGame =
            if(accept) game.joinBattle(player, character).resolvePending()
            else game.leaveBattle(character).resolvePending()

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
    suspend fun actInBattle(userId: UInt, gameId: UInt, token: String, action: PossibleBattleActions, origin: Character, target: Character?): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game
            .addActionToPending(origin, target, action)
            .resolvePending()

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
    suspend fun undoBattleAction(userId: UInt, gameId: UInt, token: String, origin: Character): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game.removeActionFromPending(origin)

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
    suspend fun leaveBattle(userId: UInt, gameId: UInt, token: String, playerCharacter: Character, action: PossibleBattleActions): Result<Game> = runCatching{

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user, gameId, this.userRepo)

        val game = gameRepo.findById(gameId)
            ?: throw GameError.GameNotFound(gameId.toInt())

        val newGame = game
            .addActionToPending(playerCharacter, null, action)
            .deleteBattle()

        gameRepo.save(newGame)
        events.publishGameUpdated(newGame)
        newGame
    }
}