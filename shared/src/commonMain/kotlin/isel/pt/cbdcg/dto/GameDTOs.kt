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
    val range: Int,
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
    val type: String
)

@Serializable
data class EvolutionDTO(
    val type: String,
    val character: String,
    val item: String? = null,
    val condition: String? = null,
    val value: Int? = null,
    val currentValue: Int? = null
)

@Serializable
data class CharacterDTO(
    val type: String,
    val name: String,
    val baseStats: String,
    val activeModifiers: Array<ModifierDTO>,
    val grade: String,
    val evolution: EvolutionDTO? = null,
    val items: Array<ItemDTO>,
    val maxItems: Int,
    val passiveType: String = "BATTLE_PASSIVE",
    val canUsePassive: Boolean = true
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
        if (passiveType != other.passiveType) return false
        if (canUsePassive != other.canUsePassive) return false

        return true
    }
    override fun hashCode(): Int {
        var result = type.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + baseStats.hashCode()
        result = 31 * result + activeModifiers.contentHashCode()
        result = 31 * result + items.contentHashCode()
        result = 31 * result + passiveType.hashCode()
        result = 31 * result + canUsePassive.hashCode()
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
    val phase: String,
    val deadline: Long
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
    val battle: BattleDTO?,
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
        if (battle != other.battle) return false

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
        result = 31 * result + (battle?.hashCode() ?: 0)
        return result
    }
}

@Serializable
data class BattleDTO(
    val characters: Array<CharacterDTO>,
    val currentTurn: Int,
    val phase: String,
    val pending: Array<BattleActionDTO>,
    val actions: Array<BattleActionDTO>,
    val itemBet: Array<BattleBetDTO>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as BattleDTO

        if (currentTurn != other.currentTurn) return false
        if (!characters.contentEquals(other.characters)) return false
        if (!pending.contentEquals(other.pending)) return false
        if (!actions.contentEquals(other.actions)) return false
        if (!itemBet.contentEquals(other.itemBet)) return false

        return true
    }
    override fun hashCode(): Int {
        var result = currentTurn
        result = 31 * result + characters.contentHashCode()
        result = 31 * result + pending.contentHashCode()
        result = 31 * result + actions.contentHashCode()
        result = 31 * result + itemBet.contentHashCode()
        return result
    }
}

@Serializable
data class BattleBetDTO(
    val player: PlayerDTO,
    val item: ItemDTO?
)

@Serializable
data class BattleActionDTO(
    val turn: Int,
    val origin: CharacterDTO,
    val target: CharacterDTO?,
    val action: String,
    val stats: String
)

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
data class SimpleGameRequestDTO(
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
data class StartBattleDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val attacker: CharacterDTO,
    val defender: CharacterDTO
)
@Serializable
data class SimpleInGameActionDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val origin: BoardTileDTO,
)

@Serializable
data class ActInBattleDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val action: String,
    val origin: CharacterDTO,
    val target: CharacterDTO?,
)

@Serializable
data class ParticipateInBattleDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val character: CharacterDTO,
    val accept: Boolean
)
