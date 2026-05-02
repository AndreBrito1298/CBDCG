package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.game.Board
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.BoardTile
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.decodeTile
import isel.pt.cbdcg.domain.game.toTurn
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val user: Int,
    val hand: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PlayerDTO

        if (user != other.user) return false
        if (!hand.contentEquals(other.hand)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = user
        result = 31 * result + hand.contentHashCode()
        return result
    }
}

@Serializable
data class GameDTO(
    val id: Int,
    val players: Array<PlayerDTO>,
    val board: Array<String>,
    val tileDeck: Array<String>,
    val turn: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameDTO

        if (id != other.id) return false
        if (!players.contentEquals(other.players)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id
        result = 31 * result + players.contentHashCode()
        return result
    }
}

fun Player.toPlayerInfo(): PlayerDTO =
    PlayerDTO(
        user = user.toInt(),
        hand = hand.map{ (idx, tile) -> "$idx|${tile.codeString()}" }.toTypedArray()
    )

fun PlayerDTO.toPlayer(): Player =
    Player(
        user = user.toUInt(),
        hand = hand.associate { string ->
            val (idx, tile) = string.split("|")
            idx.toUInt() to tile.decodeTile()
        }
    )

fun Game.toGameDTO(): GameDTO {

    val playersDTO = players.map{ it.toPlayerInfo() }
    val boardDTO = board.tiles.map{ (pos, tile) -> "${pos.coords()}|${tile.codeString()}" }
    val tileDeck = tileDeck.map{ (tile, nr) -> "${tile.codeString()}|${nr}" }.toTypedArray()

    return GameDTO(
        id = id.toInt(),
        players = playersDTO.toTypedArray(),
        board = boardDTO.toTypedArray(),
        tileDeck = tileDeck,
        turn = turn.turnString()
    )
}

// Game / Board não quer saber de "blocked", tenho de mudar alguma cena aqui? Talvez não.
fun GameDTO.toGame(): Game {

    val players = players.map{ it.toPlayer() }
    val tiles = board.map{ string ->

        val (posString, tileString) = string.split("|")
        val pos = posString.split(",").map{ it.toInt() }
        val tile = tileString.decodeTile()

        BoardTile(
            pos = BoardPosition(pos[0], pos[1]),
            tile = tile
        )
    }
    val tileDeck = tileDeck.map{ string ->
        val (tileString, nr) = string.split("|")
        val tile = tileString.decodeTile()
        tile to nr.toUInt()
    }.toMap()

    return Game(
        id = id.toUInt(),
        players = players,
        board = Board(tiles),
        tileDeck = tileDeck,
        turn = turn.toTurn()
    )

}

@Serializable
data class CreateGameDTO(
    val userId: Int,
    val token: String,
    val tableId: Int
)

@Serializable
data class PlacePieceDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val tile: String,
    val idx: Int,
    val pos: String
)

@Serializable
data class RotatePieceDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val idx: Int,
    val right: Boolean
)