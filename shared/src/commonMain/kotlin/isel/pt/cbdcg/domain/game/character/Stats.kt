package isel.pt.cbdcg.domain.game.character

data class Stats(
    val hp: UInt,
    val atk: UInt,
    val def: UInt,
    val spe: UInt
) {
    override fun toString() = "$hp&$atk&$def&$spe"
}