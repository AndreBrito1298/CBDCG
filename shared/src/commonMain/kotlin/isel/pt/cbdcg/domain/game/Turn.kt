package isel.pt.cbdcg.domain.game

class Turn(val gameTurn: UInt, val playerTurn: List<UInt>) {

    fun turnString(): String {

        val playersString = playerTurn.joinToString(","){ it.toString() }
        return "${gameTurn}|${playersString}"
    }

}

fun String.toTurn(): Turn {
    val (game, players) = split("|")

    val list = players.split(",").map{ it.toUInt() }
    return Turn(game.toUInt(), list)
}