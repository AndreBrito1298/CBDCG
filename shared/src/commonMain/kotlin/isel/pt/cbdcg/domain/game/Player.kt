package isel.pt.cbdcg.domain.game

data class Player(
    val user: UInt,
    val hand: List<Tile>
)