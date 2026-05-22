package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.error.GameError

enum class Grade{
    BASIC, RARE, EPIC, BOSS, EVOLVE, KEY
}

fun Grade.code(): String =
    when (this) {
        Grade.BASIC -> "B"
        Grade.RARE -> "R"
        Grade.EPIC -> "E"
        Grade.BOSS -> "X"
        Grade.EVOLVE -> "L"
        Grade.KEY -> "K"
    }

fun String.toGrade() =
    when(this[0]){
        'B' -> Grade.BASIC
        'R' -> Grade.RARE
        'E' -> Grade.EPIC
        'X' -> Grade.BOSS
        'L' -> Grade.EVOLVE
        'K' -> Grade.KEY
        else -> throw GameError.InvalidFormat("Grade", this)
    }