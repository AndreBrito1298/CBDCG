package isel.pt.cbdcg.repository.database.Tables.Game

import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.toDirection
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.dto.CardDTO
import isel.pt.cbdcg.dto.TileDTO
import isel.pt.cbdcg.repository.database.Tables.Game.toTile
import isel.pt.cbdcg.repository.database.Tables.Users
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import org.jetbrains.exposed.v1.json.json

object Games : IntIdTable("games") {
    val gameTurn = integer("game_turn")
    val currentPlayer = integer("current_player").references(GamePlayers.id)
    val currentTurnPhase = enumeration("turn_phase", TurnPhase::class)
    val itemDeck = json<Array<ItemJson>>("item_deck", Json.Default)
    val tileDeck = json<Array<TileJson>>("tile_deck", Json.Default)
}

@Serializable
data class ItemJson(val name: String, val quantity: UInt)

@Serializable
data class TileJson(
    val connections: Array<String>,
    val quantity: UInt
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TileDTO

        if (!connections.contentEquals(other.connections)) return false

        return true
    }
    override fun hashCode(): Int {
        return connections.contentHashCode()
    }
}

class GamesDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GamesDao>(Games)
    var gameTurn by Games.gameTurn
    var currentPlayer by Games.currentPlayer
    var currentTurnPhase by Games.currentTurnPhase
    var itemDeck by Games.itemDeck
    var tileDeck by Games.tileDeck
}

object GamePlayers : IntIdTable("game_players") {
    val gameId = integer("game_id").references(Games.id)
    val userId = integer("user_id").references(Users.id)
    val playerHands = json<Array<CardDTO>>("player_hands", Json.Default)
    val currentCharacter = varchar("current_character", 255).nullable()
}

class GamePlayersDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GamePlayersDao>(GamePlayers)
    val gameId by GamePlayers.gameId
    val userId by GamePlayers.userId
    val playerHands by GamePlayers.playerHands
    var currentCharacter by GamePlayers.currentCharacter
}

object GameSpectators : IntIdTable("game_spectators") {
    val gameId = integer("game_id").references(Games.id)
    val userId = integer("user_id").references(Users.id)
}

class GameSpectatorsDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<GameSpectatorsDao>(GameSpectators)
    var gameId by GameSpectators.gameId
    var userId by GameSpectators.userId
}

object BoardTiles : IntIdTable("board_tiles") {
    val gameId = integer("game_id").references(Games.id)
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
    val boardTileId = integer("board_tile_id").references(BoardTiles.id)
    val name = varchar("name", 255)
}

object BoardCharacterModifiers : IntIdTable("board_character_modifiers") {
    val character_name = integer("character_name").references(BoardTiles.id)
    val stats = varchar("stats", 50)
    val duration = integer("duration")
}

class BoardCharacterModifiersDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<BoardCharacterModifiersDao>(BoardCharacterModifiers)
    var characterName by BoardCharacterModifiers.character_name
    var stats by BoardCharacterModifiers.stats
    var duration by BoardCharacterModifiers.duration
}


fun TileJson.toTile(): Tile =
    Tile(
        connections = connections.map{ it.toDirection() }
    )

fun tileDeckFromDb(tiles: Array<TileJson>): Map<Tile, UInt>{
    return tiles.associate { it.toTile() to it.hashCode().toUInt() }
}

fun itemFromDb(name: String): Item =
    ItemCatalog.items.find { it.name == name } ?: error("Unknown item: $name")

fun itemDeckFromDb(items: Array<ItemJson>): Map<Item, UInt>{
    return items.associate { itemFromDb(it.name) to it.quantity }
}

fun BoardTilesDao.position(): BoardPosition = BoardPosition(x, y)