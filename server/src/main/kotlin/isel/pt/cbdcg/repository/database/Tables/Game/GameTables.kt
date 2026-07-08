package isel.pt.cbdcg.repository.database.Tables.Game

import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.PlayerHand
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.toDirection
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.toTileEffect
import isel.pt.cbdcg.domain.game.board.tile.toTileEffectDTO
import isel.pt.cbdcg.domain.game.board.tile.toTile
import isel.pt.cbdcg.domain.game.board.tile.toTileDTO
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.equipItem
import isel.pt.cbdcg.domain.game.character.getItemByName
import isel.pt.cbdcg.domain.game.character.toStats
import isel.pt.cbdcg.domain.game.toBattle
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.domain.game.toTurn
import isel.pt.cbdcg.dto.BattleDTO
import isel.pt.cbdcg.dto.CardDTO
import isel.pt.cbdcg.dto.TileDTO
import isel.pt.cbdcg.dto.TileEffectDTO
import isel.pt.cbdcg.dto.TurnDTO
import isel.pt.cbdcg.repository.database.Tables.Users
import isel.pt.cbdcg.repository.database.Tables.UsersDao
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.json.json

object Games : IntIdTable("games") {
   // val gameTurn = integer("game_turn")
    val version = uinteger("version")
    val turn = json<TurnDTO>("turn", Json.Default)
 //   val currentTurnPhase = enumeration("turn_phase", TurnPhase::class)
    val itemDeck = json<Array<ItemJson>>("item_deck", Json.Default)
    val tileDeck = json<Array<TileJson>>("tile_deck", Json.Default)
    val battle = json<BattleDTO>("battle", Json.Default).nullable()
  //  val playerTurnQueue = json<Array<Int>>("player_turn_queue", Json.Default)   // NEW
}

@Serializable
data class ItemJson(val name: String, val quantity: UInt)

@Serializable
data class TileJson(
    val connections: Array<String>,
    val quantity: UInt,
    val specialEffect: TileEffectDTO = TileEffect().toTileEffectDTO(),
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TileDTO

        return connections.contentEquals(other.connections)
    }
    override fun hashCode(): Int {
        return connections.contentHashCode()
    }
}

class GamesDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GamesDao>(Games)
   // var gameTurn by Games.gameTurn
    var version by Games.version
    var turn by Games.turn
   // var currentTurnPhase by Games.currentTurnPhase
    var itemsDeck by Games.itemDeck
    var tileDeck by Games.tileDeck
    var battle by Games.battle
}

object GamePlayers : IntIdTable("game_players") {
    val gameId = integer("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    var playerHands = json<Array<CardDTO>>("player_hands", Json.Default)
    val currentCharacter = varchar("current_character", 255).nullable()
}

class GamePlayersDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GamePlayersDao>(GamePlayers)
    var gameId by GamePlayers.gameId
    var userId by GamePlayers.userId
    var playerHands by GamePlayers.playerHands
    var currentCharacter by GamePlayers.currentCharacter
}

object GameSpectators : IntIdTable("game_spectators") {
    val gameId = integer("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val userId = integer("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
}

class GameSpectatorsDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GameSpectatorsDao>(GameSpectators)
    var gameId by GameSpectators.gameId
    var userId by GameSpectators.userId
}

object BoardTiles : IntIdTable("board_tiles") {
    val gameId = integer("game_id").references(Games.id, onDelete = ReferenceOption.CASCADE)
    val x = integer("x")
    val y = integer("y")
    val boardTiles = json<Array<TileDTO>>("board_tiles", Json.Default)
    val characterName = varchar("character_name", 255).nullable()
}

class BoardTilesDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BoardTilesDao>(BoardTiles)
    var gameId by BoardTiles.gameId
    var x by BoardTiles.x
    var y by BoardTiles.y
    var boardTiles by BoardTiles.boardTiles
    var characterName by BoardTiles.characterName
}

object BoardCharacterItems : IntIdTable("board_character_items") {
    val boardTileId = integer("board_tile_id").references(BoardTiles.id, onDelete = ReferenceOption.CASCADE)
    val name = varchar("name", 255)
}

object BoardCharacterModifiers : IntIdTable("board_character_modifiers") {
    val character_name = integer("character_name").references(BoardTiles.id, onDelete = ReferenceOption.CASCADE)
    val stats = varchar("stats", 50)
    val duration = integer("duration")
    val canUsePassive = bool("has_used_passive")
}

class BoardCharacterModifiersDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BoardCharacterModifiersDao>(BoardCharacterModifiers)
    var characterName by BoardCharacterModifiers.character_name
    var stats by BoardCharacterModifiers.stats
    var duration by BoardCharacterModifiers.duration
    var canUsePassive by BoardCharacterModifiers.canUsePassive
}


fun TileJson.toTile(): Tile =
    Tile(
        connections = connections.map{ it.toDirection() },
        specialEffect = specialEffect.toTileEffect(),
    )

fun Tile.toTileJson(): TileJson = TileJson(connections.map { it.name }.toTypedArray(), 1u, specialEffect.toTileEffectDTO())

fun Array<TileJson>.tileDeckFromDb(): Map<Tile, UInt>{
    return this.associate { it.toTile() to it.quantity }
}

fun Map<Tile, UInt>.tileDeckToDb(): Array<TileJson>{
    return this.map { TileJson(it.key.connections.map { it.name }.toTypedArray(), it.value, it.key.specialEffect.toTileEffectDTO()) }.toTypedArray()
}


fun String.itemFromDb(): Item =
    ItemCatalog.commonItems.find { it.name == this } ?: ItemCatalog.specialItems.find { it.name == this }
        ?: error("Unknown item: $this")

fun Array<ItemJson>.itemDeckFromDb(): Map<Item, UInt>{
    return this.associate { it.name.itemFromDb() to it.quantity }
}

fun Map<Item, UInt>.itemDeckToDb(): Array<ItemJson>{
    return this.map { ItemJson(it.key.name, it.value) }.toTypedArray()
}

fun BoardTilesDao.position(): BoardPosition = BoardPosition(x, y)

fun deleteBoardForGame(gameId: Int) {
    BoardTilesDao.find { BoardTiles.gameId eq gameId }.forEach { deleteBoardTile(it) }
}

private fun deleteBoardTile(boardTile: BoardTilesDao) {
    BoardCharacterItems.deleteWhere { BoardCharacterItems.boardTileId eq boardTile.id.value }
    BoardCharacterModifiersDao.find {
        BoardCharacterModifiers.character_name eq boardTile.id.value
    }.forEach { it.delete() }
    boardTile.delete()
}

fun saveBoard(gameId: Int, board: Board) {
    deleteBoardForGame(gameId)
    board.tiles.forEach { saveBoardTile(gameId, it) }
}

private fun saveBoardTile(gameId: Int, boardTile: BoardTile) {
    val saved = BoardTilesDao.new {
        this.gameId = gameId
        x = boardTile.pos.x
        y = boardTile.pos.y
        boardTiles = arrayOf(boardTile.tile.toTileDTO())
        characterName = boardTile.character?.name
    }

    val character = boardTile.character as? PlayableCharacter ?: return

    character.items.forEach { item ->
        BoardCharacterItems.insert {
            it[BoardCharacterItems.boardTileId] = saved.id.value
            it[BoardCharacterItems.name] = item.name
        }
    }

    character.activeStatModifiers.forEach { modifier ->
        BoardCharacterModifiersDao.new {
            characterName = saved.id.value
            stats = modifier.stats.toString()
            duration = modifier.duration.toInt()
            canUsePassive = character.canUsePassive
        }
    }
}


fun GamesDao.toGame(): Game {
    val gameId = id.value

    val players = GamePlayersDao
        .find { GamePlayers.gameId eq gameId }.map { it.toPlayer() }

    val spectators = GameSpectatorsDao
        .find { GameSpectators.gameId eq gameId }
        .map { it.toSpectator() }

    val board = toBoard()

    val turn = turn.toTurn()

    return Game(
        id = gameId.toUInt(),
        players = players,
        spectators = spectators,
        board = board,
        tileDeck = tileDeck.tileDeckFromDb(),
        itemDeck = itemsDeck.itemDeckFromDb(),
        turn = turn,
        battle = this.battle?.toBattle()
        //version = this.version
    )
}
fun GamesDao.toBoard(): Board {
    val tiles = BoardTilesDao
        .find { BoardTiles.gameId eq this@toBoard.id.value }
        .mapNotNull { it.toBoardTileOrNull() }
    return if (tiles.isEmpty()) Board() else Board(tiles = tiles)
}

fun GamePlayersDao.toPlayer(): Player {
    val user = userFromDb(userId)

    val hand: PlayerHand = playerHands
        .mapIndexed { index, card -> index.toUInt() to card.toCard() }
        .toMap()

    return Player(
        user = user,
        hand = hand,
        currentCharacter = currentCharacter
    )
}

fun GameSpectatorsDao.toSpectator(): Spectator =
    Spectator(user = userFromDb(userId))

private fun userFromDb(userId: Int): User =
    UsersDao.findById(userId)?.toUser()
        ?: error("User not found: $userId")

fun BoardTilesDao.toBoardTileOrNull(): BoardTile? {
    val tile = boardTiles.firstOrNull() ?: return null

    val character = characterName?.let { name -> toCharacter(name) }

    return BoardTile(
        pos = position(),
        tile = tile.toTile(),
        cooldown = 0u,
        character = character
    )
}

fun BoardTilesDao.toBoardTile(): BoardTile {
    val tile = boardTiles.firstOrNull()
        ?: error("BoardTile at ($x,$y) has no tile data")

    val character = characterName?.let { name -> toCharacter(name) }

    return BoardTile(
        pos = position(),
        tile = tile.toTile(),
        cooldown = 0u,
        character = character
    )
}

private fun BoardTilesDao.toCharacter(name: String): Character {
    val base = PlayableCharacterCatalog.basicCharacters.find { it.name == name }
        ?: PlayableCharacterCatalog.rareCharacters.find { it.name == name }?:
        PlayableCharacterCatalog.epicCharacters.find { it.name == name }
        ?: error("Unknown character: $name")
    val withItems = BoardCharacterItems
        .selectAll()
        .where { BoardCharacterItems.boardTileId eq this@toCharacter.id.value }
        .mapNotNull { row -> getItemByName(row[BoardCharacterItems.name]) }
        .fold(base) { character, item -> character.equipItem(item) }

    return BoardCharacterModifiersDao
        .find { BoardCharacterModifiers.character_name eq this@toCharacter.id.value }
        .map { it.toStatModifier() }
        .fold(withItems) { character, modifier -> character.addModifier(modifier) as PlayableCharacter }
}

fun BoardCharacterModifiersDao.toStatModifier(): StatModifier =
    StatModifier(
        stats = stats.toStats(),
        duration = duration.toUInt(),
        type = ModifierType.PERMANENT
    )
