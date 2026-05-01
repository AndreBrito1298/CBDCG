package isel.pt.cbdcg.domain

/**
 * Class that represents the role of a Player when inside the game.
 */

enum class Role{
    PLAYER, READY, SPECTATOR;
}

/**
 * Function to transform a string to a Role.
 */
fun String.toRole(): Role? {
    return when (this) {
        "PLAYER" -> Role.PLAYER
        "SPECTATOR" -> Role.SPECTATOR
        "READY" -> Role.READY
        else -> null
    }
}
