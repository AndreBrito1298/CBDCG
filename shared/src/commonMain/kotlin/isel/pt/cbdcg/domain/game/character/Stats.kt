package isel.pt.cbdcg.domain.game.character

import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4

data class Stats(
    val hp: UInt,
    val atk: UInt,
    val def: UInt,
    val spe: UInt
) {
    override fun toString() = "$hp&$atk&$def&$spe"
}

fun String.toStats(): Stats {
    val (hp, atk, def, spe) = this.split("&")
    return Stats(hp.toUInt(), atk.toUInt(), def.toUInt(), spe.toUInt())
}