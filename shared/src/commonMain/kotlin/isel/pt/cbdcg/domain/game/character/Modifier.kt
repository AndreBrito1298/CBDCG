package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.ModifierDTO

class Modifier(
    val stats: Stats,
    val positive: Boolean,
    val duration: UInt,
)

fun ModifierDTO.toModifier(): Modifier {
    val stats = this.stats.toStats()
    val positive = this.positive
    val duration = this.duration.toUInt()

    return Modifier(stats, positive, duration)
}
fun Modifier.toModifierDTO(): ModifierDTO =
    ModifierDTO(
        stats = this.stats.toString(),
        positive = this.positive,
        duration = this.duration.toInt()
    )