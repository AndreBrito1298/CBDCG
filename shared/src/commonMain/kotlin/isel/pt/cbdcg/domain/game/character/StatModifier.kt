package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.ModifierDTO

data class StatModifier(
    val stats: Stats,
    val duration: UInt,
)

fun ModifierDTO.toStatModifier(): StatModifier {
    val stats = this.stats.toStats()
    val duration = this.duration.toUInt()

    return StatModifier(stats, duration)
}
fun StatModifier.toModifierDTO(): ModifierDTO =
    ModifierDTO(
        stats = this.stats.toString(),
        duration = this.duration.toInt()
    )