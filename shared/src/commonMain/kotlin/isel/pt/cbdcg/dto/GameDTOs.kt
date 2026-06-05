package isel.pt.cbdcg.dto

import kotlinx.serialization.Serializable

@Serializable
data class TileDTO(
    val connections: Array<String>,
    val specialEffect: TileEffectDTO,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TileDTO

        if (!connections.contentEquals(other.connections)) return false
        if (specialEffect != other.specialEffect) return false

        return true
    }
    override fun hashCode(): Int {
        var result = connections.contentHashCode()
        result = 31 * result + specialEffect.hashCode()
        return result
    }
}

@Serializable
data class
TileEffectDTO(
    val type: String,
    val maxCooldown: Int,
    val info: String
)
@Serializable
data class CardDTO(
    val type: String,
    val tile: TileDTO?,
    val character: CharacterDTO?,
    val item: ItemDTO?
)

@Serializable
data class PlayerDTO(
    val user: UserDTO,
    val hand: Array<CardDTO>,
    val currentCharacter: String?,
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
    val duration: Int,
)

@Serializable
data class CharacterDTO(
    val type: String,
    val name: String,
    val baseStats: String,
    val activeModifiers: Array<ModifierDTO>,
    val grade: String,
    val items: Array<ItemDTO>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CharacterDTO

        if (type != other.type) return false
        if (name != other.name) return false
        if (baseStats != other.baseStats) return false
        if (!activeModifiers.contentEquals(other.activeModifiers)) return false
        if (!items.contentEquals(other.items)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + baseStats.hashCode()
        result = 31 * result + activeModifiers.contentHashCode()
        result = 31 * result + items.contentHashCode()
        return result
    }
}

@Serializable
data class ItemDTO(
    val name: String,
    val stats: String,
    val grade: String
)

@Serializable
data class BoardTileDTO(
    val pos: String,
    val tile: TileDTO,
    val cooldown: Int?,
    val character: CharacterDTO?
)

@Serializable
data class TileDeckDTO(
    val tile: TileDTO,
    val copies: Int
)

@Serializable
data class ItemDeckDTO(
    val item: ItemDTO,
    val copies: Int
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
    val itemDeck: Array<ItemDeckDTO>,
    val turn: TurnDTO,
    val winner: PlayerDTO?
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameDTO

        if (id != other.id) return false
        if (!players.contentEquals(other.players)) return false
        if (!spectators.contentEquals(other.spectators)) return false
        if (!board.contentEquals(other.board)) return false
        if (!tileDeck.contentEquals(other.tileDeck)) return false
        if (!itemDeck.contentEquals(other.itemDeck)) return false
        if (turn != other.turn) return false
        if (winner != other.winner) return false

        return true
    }
    override fun hashCode(): Int {
        var result = id
        result = 31 * result + players.contentHashCode()
        result = 31 * result + spectators.contentHashCode()
        result = 31 * result + board.contentHashCode()
        result = 31 * result + tileDeck.contentHashCode()
        result = 31 * result + itemDeck.contentHashCode()
        result = 31 * result + turn.hashCode()
        result = 31 * result + (winner?.hashCode() ?: 0)
        return result
    }
}

@Serializable
data class CreateGameDTO(
    val userId: Int,
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

@Serializable
data class BoardTileEffectDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val updaterName: String,
    val origin: BoardTileDTO,
    val target: Array<BoardTileDTO>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BoardTileEffectDTO

        if (userId != other.userId) return false
        if (gameId != other.gameId) return false
        if (token != other.token) return false
        if (updaterName != other.updaterName) return false
        if (origin != other.origin) return false
        if (!target.contentEquals(other.target)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = userId
        result = 31 * result + gameId
        result = 31 * result + token.hashCode()
        result = 31 * result + updaterName.hashCode()
        result = 31 * result + origin.hashCode()
        result = 31 * result + target.contentHashCode()
        return result
    }
}

@Serializable
data class LeaveGameDTO(
    val userId: Int,
    val gameId: Int,
    val token: String
)

@Serializable
data class UnequipItemDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val character: CharacterDTO,
    val index: Int,
)



// Will be changed

@Serializable
data class DrawItemDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val origin: BoardTileDTO,
)
@Serializable
data class UpdateModifiersDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val origin: BoardTileDTO,
)