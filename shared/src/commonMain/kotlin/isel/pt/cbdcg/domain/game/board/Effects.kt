package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.CardType
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.PlayerHand
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.addToHand
import isel.pt.cbdcg.domain.game.board.characterMovementSAM
import isel.pt.cbdcg.domain.game.removeFromHand
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.GameError
import kotlin.random.Random
import kotlin.reflect.typeOf

interface Entity

enum class UpdaterName {
    MOVEMENT,
    BATTLE,
    SWAMP
}

sealed interface EffectResult<out T : Entity> {
    data class One<T : Entity>(val value: T) : EffectResult<T>
    data class Many<T : Entity>(val values: List<T>) : EffectResult<T>
}

fun interface Updater<T : Entity> {
    fun Game.apply(origin: T, vararg targets: T?): EffectResult<T>

}

class CharacterMovement: Updater<BoardTile> {
    override fun Game.apply(
        origin: BoardTile, vararg targets: BoardTile?): EffectResult.Many<BoardTile> {
        if(this.turn.phase != TurnPhase.MOVEMENT) throw GameError.CharacterMovementRestriction()
        val newStartingTile = origin.copy(character = null)
        val endTile = targets.first() ?: throw BoardError.NoTargetFound()
        if(endTile.character != null) throw BoardError.TileOccupied()
        val newEndingTile = targets.first()?.copy(character = origin.character)
            ?: throw BoardError.NoTargetFound()
        return EffectResult.Many(listOf(newStartingTile, newEndingTile))
    }
}

val characterMovementSAM =
    Updater<BoardTile> { origin, targets ->
        if(this.turn.phase != TurnPhase.MOVEMENT) throw GameError.CharacterMovementRestriction()
        val newStartingTile = origin.copy(character = null)
        val endTile = targets.first() ?: throw BoardError.NoTargetFound()
        if (endTile.character != null) throw BoardError.TileOccupied()
        val newEndingTile = targets.first()?.copy(character = origin.character)
            ?: throw BoardError.NoTargetFound()
        EffectResult.Many(listOf(newStartingTile, newEndingTile))
}

val stealSAM =
    Updater<Player> { playerTaking, playerLosing ->
    if(playerLosing.first()!!.hand.size >3) EffectResult.Many(listOf())
    val availableCards =  playerLosing.first()!!.hand
        .filter { it.value.type != CardType.CHARACTER }
    val takenCardIdx = availableCards.keys.random()
    val playerWithoutCard = playerLosing.first()!!.removeFromHand(takenCardIdx)
    val playerWithExtraCard = playerTaking.addToHand(availableCards[takenCardIdx]!!)
    EffectResult.Many(listOf(playerWithoutCard, playerWithExtraCard))
}

val swampSAM =
    Updater<Board> { board, nul ->
        val tile1 = board.tiles.random()
        val tile2 = board.tiles.random()
        val resBoard = board.tiles.toMutableList().apply { removeAll { it == tile1 || it == tile2 } }
        resBoard.addAll(listOf(tile1.copy(pos = tile2.pos), tile2.copy(pos = tile1.pos)))
        EffectResult.One(board.copy(tiles = resBoard))
}

class swap(): Updater<Board> {
    override fun Game.apply(
        origin: Board,
        vararg targets: Board?
    ): EffectResult<Board> {
        val tile1 = origin.tiles.random()
        val tile2 = origin.tiles.random()
        val resBoard = origin.tiles.toMutableList().apply { removeAll { it == tile1 || it == tile2 } }
        resBoard.addAll(listOf(tile1.copy(pos = tile2.pos), tile2.copy(pos = tile1.pos)))
        return EffectResult.One(origin.copy(tiles = resBoard))
    }
}

fun Game.applyBoardTileUpdater(updater: Updater<BoardTile>, origin: BoardTile, vararg targets: BoardTile): Game {
    val g = this
    val result = updater.run { g.apply(origin, *targets) }
    return copy(board = board.applyBoardTileUpdater(result))
}

fun main(){
    listOf(stealSAM,characterMovementSAM)
}