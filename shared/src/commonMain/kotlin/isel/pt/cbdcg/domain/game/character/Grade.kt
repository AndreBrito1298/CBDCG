package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.BASIC_POINTS
import isel.pt.cbdcg.EPIC_POINTS
import isel.pt.cbdcg.EVOLVE_POINTS
import isel.pt.cbdcg.KEY_POINTS
import isel.pt.cbdcg.RARE_POINTS
import isel.pt.cbdcg.error.GameError

enum class Grade{
    EVOLVE, BASIC, RARE, EPIC, KEY
}

fun Grade.special(): Boolean = this == Grade.EVOLVE || this == Grade.KEY
fun Grade.isEqualOrHigherThan(other: Grade): Boolean = this.ordinal >= other.ordinal
fun Grade.points(): Int = when(this){
    Grade.EVOLVE -> EVOLVE_POINTS
    Grade.BASIC -> BASIC_POINTS
    Grade.RARE -> RARE_POINTS
    Grade.EPIC -> EPIC_POINTS
    Grade.KEY -> KEY_POINTS
}

fun Grade.code(): String =
    when (this) {
        Grade.BASIC -> "B"
        Grade.RARE -> "R"
        Grade.EPIC -> "E"
        Grade.EVOLVE -> "L"
        Grade.KEY -> "K"
    }

fun String.toGrade() =
    when(this[0]){
        'B' -> Grade.BASIC
        'R' -> Grade.RARE
        'E' -> Grade.EPIC
        'L' -> Grade.EVOLVE
        'K' -> Grade.KEY
        else -> throw GameError.InvalidFormat("Grade", this)
    }