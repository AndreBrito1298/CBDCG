package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.board.decodeTile
import isel.pt.cbdcg.domain.game.board.toPosition
import isel.pt.cbdcg.domain.game.character.decodeCharacter
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.domain.game.decodeCard
import isel.pt.cbdcg.domain.game.toTurn
import isel.pt.cbdcg.error.GameError
import kotlinx.serialization.Serializable

@Serializable
data class PlayerDTO(
    val user: UserDTO,
    val hand: Array<String>,
    val currentCharacter: String,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PlayerDTO

        if (user != other.user) return false
        if (!hand.contentEquals(other.hand)) return false
        if (currentCharacter != other.currentCharacter) return false

        return true
    }
    override fun hashCode(): Int {
        var result = user.hashCode()
        result = 31 * result + hand.contentHashCode()
        result = 31 * result + currentCharacter.hashCode()
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
            },
            currentCharacter =
                if(currentCharacter.isNotBlank()) currentCharacter.decodeCharacter()
                else null
        )
}

@Serializable
data class SpectatorDTO(
    val user: UserDTO,
){

    fun toSpectator(): Spectator = Spectator(this.user.toUser())
}

@Serializable
data class BoardTileDTO(
    val pos: String,
    val tile: String,
    val character: String
)

@Serializable
data class GameDTO(
    val id: Int,
    val players: Array<PlayerDTO>,
    val spectators: Array<SpectatorDTO>,
    val board: Array<BoardTileDTO>,
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
        val tiles = board.map{ (posString, tileString, string) ->

            val pos = posString.toPosition()
            val tile = tileString.decodeTile()
            val character = if(string.isNotBlank()) string.toCharacter()
                            else null

            BoardTile(
                pos = pos,
                tile = tile,
                character = character
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

@Serializable
data class NextPhaseDTO(
    val userId: Int,
    val gameId: Int,
    val token: String
)