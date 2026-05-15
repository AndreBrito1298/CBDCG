package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.GameError

enum class TurnPhase{
    CONSTRUCTION, SUBSTITUTION, MOVEMENT
}

fun String.toTurnPhase(): TurnPhase =
    when (this[0]) {
        'C' -> TurnPhase.CONSTRUCTION
        'S' -> TurnPhase.SUBSTITUTION
        'M' -> TurnPhase.MOVEMENT
        else -> throw GameError.InvalidTurnPhase(this)
    }

class Turn(val gameTurn: UInt, val playerTurn: List<UInt>, val phase: TurnPhase) {

    override fun toString(): String {
        val playersString = playerTurn.joinToString(","){ it.toString() }
        return "${gameTurn}|${phase.name[0]}|${playersString}"
    }

}

fun String.toTurn(): Turn {
    val (game, phase, players) = split("|")
    val list = players.split(",").map{ it.toUInt() }
    return Turn(game.toUInt(),  list, phase.toTurnPhase())
}