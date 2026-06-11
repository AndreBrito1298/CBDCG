package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.GameError


interface Entity{
    fun applyToGame(game: Game): Game
}

private val funToNameMap = listOf(
    CharacterMovement,
    swap,
    playerEffect,
    ).associateBy { it::class.simpleName!! }

fun interface GameUpdater<T: Entity>{
    fun apply(game: Game, entity: Entity): Game
}

object PlayerUpdater: GameUpdater<Player>{
    override fun apply(game: Game, entity: Entity):Game = entity.applyToGame(game)
}

object BoardUpdater: GameUpdater<Board>{
    override fun apply(game: Game, entity: Entity):Game = entity.applyToGame(game)
}

object BoardTileUpdater: GameUpdater<BoardTile>{
    override fun apply(game: Game, entity: Entity):Game = entity.applyToGame(game)
}

private val gameUpdaters =
    mapOf(
        Player::class to PlayerUpdater,
        Board::class to BoardUpdater,
        BoardTile::class to BoardTileUpdater,
    )




private interface UpdaterE<T: Entity, R: Entity> {
    fun Game.apply(origin: T, targets: List<R>): Pair<T,List<R>>
   fun Game.applyWithEntity(origin: Entity, targets: List<Entity>): Game{
        val list = targets.map { it as R }
        val g = this
        val result = this@UpdaterE.run { g.apply(origin as T, list) }
        return g.applyToGame(result as Pair<Entity, List<Entity>>)
    }

    private fun Game.applyToGame(
        result: Pair<Entity, List<Entity>>
    ): Game {
        val gameUpdaterO = gameUpdaters[result.first::class] ?: throw IllegalArgumentException("Updater not found")
        val gameUpdaterT = gameUpdaters[result.second.first()::class] ?: throw IllegalArgumentException("Updater not found")
        var game = gameUpdaterO.apply(this, result.first)
        result.second.forEach {
            game = gameUpdaterT.apply(game, it)
        }
        return game
    }

}
fun Game.gameUpdateByName(name: String, origin: Entity, targets: List<Entity>): Game{
    val updater = funToNameMap[name] ?: throw IllegalArgumentException("Updater not found")
    return updater.run { this@gameUpdateByName.applyWithEntity(origin, targets) }
}

object CharacterMovement : UpdaterE<BoardTile, BoardTile> {

    override fun Game.apply(origin: BoardTile, targets: List<BoardTile>): Pair<BoardTile, List<BoardTile>> {
        if(this.turn.phase != TurnPhase.MOVEMENT) throw GameError.CharacterMovementRestriction()
        val newStartingTile = origin.copy(character = null)
        val endTile = targets.first() ?: throw BoardError.NoTargetFound()
        if(endTile.character != null) throw BoardError.TileOccupied()
        val newEndingTile = targets.first()?.copy(character = origin.character)
            ?: throw BoardError.NoTargetFound()
        return Pair(newStartingTile, listOf(newEndingTile))
    }


}

object swap: UpdaterE<Board, Board>  {
    override fun Game.apply(
        origin: Board,
        targets: List<Board>
    ): Pair<Board, List<Board>> {
        val tile1 = origin.tiles.random()
        val tile2 = origin.tiles.random()
        val resBoard = origin.tiles.toMutableList().apply { removeAll { it == tile1 || it == tile2 } }
        resBoard.addAll(listOf(tile1.copy(pos = tile2.pos), tile2.copy(pos = tile1.pos)))
        TODO("Not yet implemented")
    }
}

object playerEffect: UpdaterE<Player, Player> {
    override fun Game.apply(origin: Player, targets: List<Player>): Pair<Player, List<Player>> {
        TODO("Not yet implemented")
    }
}

/*
 private fun Game.applyToGame(result: Pair<Entity, List<Entity>>): Game{
        val oType = originType::class.simpleName?:"null"
        var game = this
        when(oType.toEffectType()){
            EffectType.Player -> {
                val p = result.first as Player
                val list = game.players.toMutableList()
                list.removeAll { it.user != p.user }
                list.add(p)
                game = game.copy(players = list)
            }
            EffectType.Board -> {game = game.copy(board = result.first as Board)}
            EffectType.BoardTile -> {
                game = game.copy(board = game.board.
                applyBoardTileUpdater(EffectResult.One(result.first as BoardTile)))
            }
        }
        val tType = targetType::class.simpleName?:"null"
        when(tType.toEffectType()){
            EffectType.Player -> {
                val players = game.players.toMutableList()
                result.second.forEach { it ->
                    val p = it as Player
                    players.removeAll { it.user != p.user }
                    players.add(p)
                }
                game = game.copy(players = players)}
            EffectType.Board -> {}
            EffectType.BoardTile -> {
                game = game.copy(board = game.board.
                applyBoardTileUpdater(EffectResult.Many(result.second.map { it as BoardTile } )))
            }
        }
        return game
    }
 */