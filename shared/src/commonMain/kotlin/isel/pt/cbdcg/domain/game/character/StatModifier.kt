package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.ModifierDTO
import isel.pt.cbdcg.error.GameError

enum class ModifierType{
    TILE_EFFECT, BATTLE
}

fun String.toModifierType(): ModifierType =
    when(this){
        "TILE_EFFECT" -> ModifierType.TILE_EFFECT
        "BATTLE" -> ModifierType.BATTLE
        else -> throw GameError.InvalidFormat("Modifier Type", this)
    }

data class StatModifier(
    val stats: Stats,
    val duration: Int,
    val type: ModifierType
)

fun ModifierDTO.toStatModifier(): StatModifier =
    StatModifier(
        stats = stats.toStats(),
        duration = duration,
        type = type.toModifierType()
    )

fun StatModifier.toModifierDTO(): ModifierDTO =
    ModifierDTO(
        stats = stats.toString(),
        duration = duration,
        type = type.name
    )