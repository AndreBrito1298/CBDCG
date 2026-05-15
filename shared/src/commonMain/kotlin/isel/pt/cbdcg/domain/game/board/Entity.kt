package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.character.Character

interface Entity

sealed interface EffectResult<out T : Entity> {
    data class One<T : Entity>(val value: T) : EffectResult<T>
    data class Many<T : Entity>(val values: List<T>) : EffectResult<T>
}

fun interface Effect<T : Entity> {
    fun apply(origin: T, vararg targets: BoardTile): EffectResult<BoardTile>
}

class movement(): Effect<BoardTile> {
    override fun apply(
        origin: BoardTile,
        vararg targets: BoardTile
    ): EffectResult.Many<BoardTile> {
        val from = origin.removeCharacter()
        if(origin.character != null && targets.isNotEmpty()) {
            val to = targets.first().addCharacter(origin.character)
            return EffectResult.Many(listOf(from, to))
        }
        else{
            TODO()
        }
    }
}

class attack(): Effect<Character> {
    override fun apply(
        origin: Character,
        vararg targets: BoardTile
    ): EffectResult.Many<BoardTile> {
        val res = mutableListOf<BoardTile>()
        targets.forEach {
            val newHp = it.character!!.stats.hp - (origin.stats.atk-origin.stats.def)
            val newStats = it.character.stats.copy(hp = newHp)
            res.add(it.copy(character = it.character.editStats(newStats)))
       }
        return EffectResult.Many(res)
    }
}