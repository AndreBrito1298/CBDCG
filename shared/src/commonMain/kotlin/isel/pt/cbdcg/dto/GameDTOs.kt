package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.game.Board
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.BoardTile
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.decodeTile
import isel.pt.cbdcg.domain.game.codeString
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val user: Int,
    val turn: Int,
)
@Serializable
data class GameDTO(
    val id: Int,
    val players: Array<PlayerDTO>,
    val board: Array<String>,
    val turn: Int
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
        turn = turn.toInt()
    )

fun PlayerDTO.toPlayer(): Player =
    Player(
        user = user.toUInt(),
        turn = turn.toUInt()
    )

fun Game.toGameDTO(): GameDTO {

    val playersDTO = players.map{ it.toPlayerInfo() }
    val boardDTO = board.tiles.map{ (pos, tile) -> "${pos.x},${pos.y}|${tile.codeString()}" }

    return GameDTO(
        id = id.toInt(),
        players = playersDTO.toTypedArray(),
        board = boardDTO.toTypedArray(),
        turn = turn.toInt()
    )
}

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

    return Game(
        id = id.toUInt(),
        players = players,
        board = Board(tiles),
        turn = turn.toUInt()
    )

}

@Serializable
data class CreateGameDTO(
    val userId: Int,
    val token: String,
    val tableId: Int
)