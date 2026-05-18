package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class TileDTO(
    val connections: Array<String>
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

@Serializable
data class CardDTO(
    val type: String,
    val tile: TileDTO?,
    val character: CharacterDTO?
)

@Serializable
data class PlayerDTO(
    val user: UserDTO,
    val hand: Array<CardDTO>,
    val currentCharacter: CharacterDTO?,
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
}

@Serializable
data class SpectatorDTO(
    val user: UserDTO,
)

@Serializable
data class ModifierDTO(
    val stats: String,
    val positive: Boolean,
    val duration: Int,
)

@Serializable
data class CharacterDTO(
    val type: String,
    val name: String,
    val baseStats: String,
    val activeModifiers: Array<ModifierDTO>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CharacterDTO

        if (name != other.name) return false
        if (baseStats != other.baseStats) return false
        if (!activeModifiers.contentEquals(other.activeModifiers)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = name.hashCode()
        result = 31 * result + baseStats.hashCode()
        result = 31 * result + activeModifiers.contentHashCode()
        return result
    }
}

@Serializable
data class BoardTileDTO(
    val pos: String,
    val tile: TileDTO,
    val character: CharacterDTO?
)

@Serializable
data class TileDeckDTO(
    val idx: Int,
    val tile: TileDTO
)

@Serializable
data class TurnDTO(
    val gameTurn: Int,
    val playerTurn: Array<Int>,
    val phase: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TurnDTO

        if (gameTurn != other.gameTurn) return false
        if (!playerTurn.contentEquals(other.playerTurn)) return false
        if (phase != other.phase) return false

        return true
    }
    override fun hashCode(): Int {
        var result = gameTurn
        result = 31 * result + playerTurn.contentHashCode()
        result = 31 * result + phase.hashCode()
        return result
    }
}

@Serializable
data class GameDTO(
    val id: Int,
    val players: Array<PlayerDTO>,
    val spectators: Array<SpectatorDTO>,
    val board: Array<BoardTileDTO>,
    val tileDeck: Array<TileDeckDTO>,
    val turn: TurnDTO
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
    val card: CardDTO,
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