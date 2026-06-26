package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.error.GameError

enum class Grade{
    EVOLVE, BASIC, RARE, EPIC, KEY
}

fun Grade.special(): Boolean = this == Grade.EVOLVE || this == Grade.KEY
fun Grade.isEqualOrHigherThan(other: Grade): Boolean = this.ordinal >= other.ordinal

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