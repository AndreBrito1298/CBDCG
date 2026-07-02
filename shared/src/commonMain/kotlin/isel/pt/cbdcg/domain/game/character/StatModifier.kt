package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.ModifierDTO
import isel.pt.cbdcg.error.GameError

enum class ModifierType{
    TILE_EFFECT,
    BATTLE_ATTACK,
    BATTLE_HOLD,
    BATTLE_FLEE,
    PERMANENT,

    PASSIVE_MODIFIER,
}

fun ModifierType.isBattleMod(): Boolean =
    this == ModifierType.BATTLE_ATTACK || this == ModifierType.BATTLE_HOLD || this == ModifierType.BATTLE_FLEE

fun String.toModifierType(): ModifierType =
    when(this){
        "TILE_EFFECT" -> ModifierType.TILE_EFFECT
        "BATTLE_ATTACK" -> ModifierType.BATTLE_ATTACK
        "BATTLE_HOLD" -> ModifierType.BATTLE_HOLD
        "BATTLE_FLEE" -> ModifierType.BATTLE_FLEE
        "PERMANENT" -> ModifierType.PERMANENT
        else -> throw GameError.InvalidFormat("Modifier Type", this)
    }

data class StatModifier(
    val stats: Stats,
    val duration: UInt,
    val type: ModifierType
)

fun ModifierDTO.toStatModifier(): StatModifier =
    StatModifier(
        stats = stats.toStats(),
        duration = duration.toUInt(),
        type = type.toModifierType()
    )

fun StatModifier.toModifierDTO(): ModifierDTO =
    ModifierDTO(
        stats = stats.toString(),
        duration = duration.toInt(),
        type = type.name
    )