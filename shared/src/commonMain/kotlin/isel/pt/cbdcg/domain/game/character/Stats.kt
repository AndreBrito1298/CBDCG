package isel.pt.cbdcg.domain.game.character

import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4

data class Stats(
    val hp: Int,
    val atk: Int,
    val def: Int,
    val spe: Int
) {
    override fun toString() = "$hp&$atk&$def&$spe"
}

fun String.toStats(): Stats {
    val (hp, atk, def, spe) = this.split("&")
    return Stats(hp.toInt(), atk.toInt(), def.toInt(), spe.toInt())
}