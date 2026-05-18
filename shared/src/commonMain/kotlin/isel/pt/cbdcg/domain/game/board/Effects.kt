package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Player

interface Entity

sealed interface EffectResult<out T : Entity> {
    data class One<T : Entity>(val value: T) : EffectResult<T>
    data class Many<T : Entity>(val values: List<T>) : EffectResult<T>
}

fun interface Effect<T : Entity> {
    fun apply(origin: T, vararg targets: BoardTile?): EffectResult<T>
}

class movement(): Effect<BoardTile> {
    override fun apply(
        origin: BoardTile,
        vararg targets: BoardTile?
    ): EffectResult.Many<BoardTile> {
        val from = origin.removeCharacter()
        if(origin.character != null && targets.isNotEmpty()) {
            val to = targets.first()!!.addCharacter(origin.character)
            return EffectResult.Many(listOf(from, to))
        }
        else{
            TODO()
        }
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
        if(origin.currentCharacter!!.baseStats.spe<speed && origin.hand.isNotEmpty()){
            if(speed == 3u){
                //origin.hand.filter { it.value.type == CardType.Item }
                // val p = origin.removeFromHand(Random.nextInt(1, origin.hand.size).toUInt())
                //return EffectResult.One()
            }
            else if(speed == 4u){
                TODO()
            }
        }
        return EffectResult.One(origin)
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
            val newHp = it!!.character!!.baseStats.hp+(hpMod?:0u) - (origin.character!!.baseStats.atk+(attackMod?:0u)-origin.character.baseStats.def)
            val newStats = it.character.baseStats.copy(hp = newHp)
            // res.add(it.copy(character = it.character.editStats(newStats)))
        }
        return EffectResult.Many(res)
    }
}


