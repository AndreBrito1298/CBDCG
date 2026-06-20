package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.MAX_STAT_VALUE
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.component3
import kotlin.collections.component4

data class Stats(
    val hp: Int = 0,
    val dmg: Int = 0,
    val def: Int = 0,
    val spe: Int = 0
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

fun Stats.bounded(
    minHp: Int = 0,
    minDmg: Int = 0,
    minDef: Int = 0,
    minSpe: Int = 1,
    max: Int = MAX_STAT_VALUE
) = copy(
    hp = hp.coerceIn(minHp, max),
    dmg = dmg.coerceIn(minDmg, max),
    def = def.coerceIn(minDef, max),
    spe = spe.coerceIn(minSpe, max),
)