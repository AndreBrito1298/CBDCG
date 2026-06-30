package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.dto.TurnDTO
import isel.pt.cbdcg.error.GameError
import kotlin.time.Clock

enum class TurnPhase{
    CONSTRUCTION, SUBSTITUTION, MOVEMENT
}

fun String.toTurnPhase(): TurnPhase =
    when (this[0]) {
        'C' -> TurnPhase.CONSTRUCTION
        'S' -> TurnPhase.SUBSTITUTION
        'M' -> TurnPhase.MOVEMENT
        else -> throw GameError.InvalidFormat("Turn Phase", this)
    }

data class Turn(
    val gameTurn: UInt,
    val playerTurn: List<UInt>,
    val phase: TurnPhase,
    val deadline: Long
) {

    override fun toString(): String {
        val playersString = playerTurn.joinToString(","){ it.toString() }
        return "${gameTurn}|${phase.name[0]}|${playersString}"
    }

}
fun newDeadline(seconds: Int): Long = Clock.System.now().toEpochMilliseconds() + seconds * 1000L
fun Turn.extendDeadline(seconds: Int): Long = deadline + seconds * 1000L

fun Turn.toTurnDTO(): TurnDTO =
    TurnDTO(
        gameTurn = gameTurn.toInt(),
        playerTurn = playerTurn.map{ it.toInt() }.toTypedArray(),
        phase = phase.name,
        deadline = deadline
    )

fun TurnDTO.toTurn(): Turn =
    Turn(
        gameTurn = gameTurn.toUInt(),
        playerTurn = playerTurn.map{ it.toUInt() },
        phase = phase.toTurnPhase(),
        deadline = deadline
    )