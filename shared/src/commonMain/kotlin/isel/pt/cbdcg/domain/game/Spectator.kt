package isel.pt.cbdcg.domain.game

data class Spectator(
    val user: UInt
) {
    fun toSpectatorInfo(): String = user.toString()
}

fun String.toSpectator(): Spectator = Spectator(this.toUInt())