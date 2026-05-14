package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.board.decodeTile
import isel.pt.cbdcg.domain.game.decodeCard
import isel.pt.cbdcg.domain.game.toTurn
import isel.pt.cbdcg.error.GameError
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val user: UserDTO,
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
        var result = user.hashCode()
        result = 31 * result + hand.contentHashCode()
        return result
    }

    fun toPlayer(): Player =
        Player(
            user = user.toUser(),
            hand = hand.associate { string ->

                val (idx, type, value) = string.split("|")
                val card = value.decodeCard(type[0])
                    ?: throw GameError.CardDoesNotExist(string)

                idx.toUInt() to card
            }
        )

}

@Serializable
data class SpectatorDTO(
    val user: UserDTO,
){

    fun toSpectator(): Spectator = Spectator(this.user.toUser())
}

@Serializable
data class GameDTO(
    val id: Int,
    val players: Array<PlayerDTO>,
    val spectators: Array<SpectatorDTO>,
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

    fun toGame(): Game {

        val players = players.map{ it.toPlayer() }
        val spectators = spectators.map{ it.toSpectator() }
        val tiles = board.map{ string ->

            val (posString, tileString) = string.split("|")
            val pos = posString.split(",").map{ it.toInt() }
            val tile = tileString.decodeTile()

            BoardTile(
                pos = BoardPosition(pos[0], pos[1]),
                tile = tile
            )
        }
        val tileDeck = tileDeck.associate { string ->
            val (tileString, nr) = string.split("|")
            val tile = tileString.decodeTile()
            tile to nr.toUInt()
        }

        return Game(
            id = id.toUInt(),
            players = players,
            spectators = spectators,
            board = Board(tiles),
            tileDeck = tileDeck,
            turn = turn.toTurn()
        )

    }
}


@Serializable
data class CreateGameDTO(
    val userId: Int,
    val token: String,
    val tableId: Int
)

@Serializable
data class PlaceOnBoardDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val card: String,
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