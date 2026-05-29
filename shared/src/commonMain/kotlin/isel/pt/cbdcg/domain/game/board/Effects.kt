package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.error.BoardError

interface Entity

sealed interface EffectResult<out T : Entity> {
    data class One<T : Entity>(val value: T) : EffectResult<T>
    data class Many<T : Entity>(val values: List<T>) : EffectResult<T>
}

fun interface Effect<T : Entity> {
    fun apply(origin: T, vararg targets: BoardTile?): EffectResult<T>
}

class CharacterMovement: Effect<BoardTile> {
    override fun apply(
        origin: BoardTile,
        vararg targets: BoardTile?
    ): EffectResult.Many<BoardTile> {

        val newStartingTile = origin.copy(character = null)

        val endTile = targets.first() ?: throw BoardError.NoTargetFound()
        if(endTile.character != null) throw BoardError.TileOccupied()

        val newEndingTile = targets.first()?.copy(character = origin.character)
            ?: throw BoardError.NoTargetFound()

        return EffectResult.Many(listOf(newStartingTile, newEndingTile))
    }
}


class swap(): Effect<Board> {
    override fun apply(
        origin: Board,
        vararg targets: BoardTile?
    ): EffectResult<Board> {
        val tile1 = origin.tiles.random()
        val tile2 = origin.tiles.random()
        val resBoard = origin.tiles.toMutableList().apply { removeAll { it == tile1 || it == tile2 } }
        resBoard.addAll(listOf(tile1.copy(pos = tile2.pos), tile2.copy(pos = tile1.pos)))
        return EffectResult.One(origin.copy(tiles = resBoard))
    }
}

class steal(val speed: UInt): Effect<Player> {
    override fun apply(
        origin: Player,
        vararg targets: BoardTile?
    ): EffectResult<Player> {
        TODO()
    }
}

class attack(
    val attackMod: UInt? = null,
    val hpMod: UInt? = null,
    val evadeMod: UInt? = null,
): Effect<BoardTile> {
    override fun apply(
        origin: BoardTile,
        vararg targets: BoardTile?
    ): EffectResult.Many<BoardTile> {
        val res = mutableListOf<BoardTile>()
        targets.forEach {
            //   val evade = Random()
            val newHp = it!!.character!!.baseStats.hp+(hpMod?.toInt()?:0) - (origin.character!!.baseStats.atk+(attackMod?.toInt()?:0)-origin.character.baseStats.def)
            val newStats = it.character.baseStats.copy(hp = newHp)
            // res.add(it.copy(character = it.character.editStats(newStats)))
        }
        return EffectResult.Many(res)
    }
}


