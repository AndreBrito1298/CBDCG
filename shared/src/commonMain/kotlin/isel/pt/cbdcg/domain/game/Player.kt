package isel.pt.cbdcg.domain.game

// Since one user can be at most in one game at a time, it is not necessary to specify the Game
data class Player(
    val user: UInt,
    val turn: UInt
)