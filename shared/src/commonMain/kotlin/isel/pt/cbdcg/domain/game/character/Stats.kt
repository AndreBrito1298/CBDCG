package isel.pt.cbdcg.domain.game.character

import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4

data class Stats(
    val hp: Int,
    val dmg: Int,
    val def: Int,
    val spe: Int
) {
    override fun toString() = "$hp&$dmg&$def&$spe"
}

fun String.toStats(): Stats {
    val (hp, atk, def, spe) = this.split("&")
    return Stats(hp.toInt(), atk.toInt(), def.toInt(), spe.toInt())
}

operator fun Stats.plus(other: Stats): Stats =
    copy(
        hp = hp + other.hp,
        dmg = dmg + other.dmg,
        def = def + other.def,
        spe = spe + other.spe,
    )