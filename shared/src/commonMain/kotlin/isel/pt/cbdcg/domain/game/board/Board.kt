package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.error.BoardPlacementError.*
import isel.pt.cbdcg.error.GameError
import kotlin.collections.plus

typealias BoardTiles = List<BoardTile>

data class Board(
    val tiles: BoardTiles = listOf(
        BoardTile(
            pos = BoardPosition(0,0),
            tile = Tile(Direction.entries),
            character = null
        )
    )
): Entity
fun Board.checkBlocked(position: BoardPosition, tile: Tile){

    val adjTiles = tile.getAdjacent(tiles, position)
    val blocked = tile.getBlocked(adjTiles)

    if(adjTiles.all{ (dir,_) -> blocked.contains(dir) })
        throw TileConnectionMismatch()
}
fun Board.placeTile(position: BoardPosition, tile: Tile, turnPhase: TurnPhase): Board {
    if(turnPhase != TurnPhase.CONSTRUCTION)
        throw GameError.TilePlacementRestriction()

    if (tiles.any { it.pos == position })
        throw PositionTaken(position.x, position.y)

    checkBlocked(position, tile)

    return copy(tiles = tiles + BoardTile(position, tile, null))
}

fun Board.placeCharacter(position: BoardPosition, character: PlayableCharacter, phase: TurnPhase): Board {

    if(tiles.any{ it.pos == position && it.character != null })
        throw GameError.TileOccupied()

    val newBoard = tiles.map{ boardTile ->
        if(boardTile.pos == position){
            boardTile.addCharacter(character)
        } else boardTile
    }

    return copy(tiles = newBoard)
}

fun Board.applyBoardTileEffect(result: EffectResult<BoardTile>): Board =
    when (result) {
        is EffectResult.One -> replaceBoardTile(result.value)
        is EffectResult.Many -> result.values.fold(this) { board, updatedTile -> board.replaceBoardTile(updatedTile) }
    }
private fun Board.replaceBoardTile(updatedTile: BoardTile): Board {
    val newTiles = tiles.filterNot { it.pos == updatedTile.pos }.toMutableList()
    newTiles.add(updatedTile)
    return copy(tiles = newTiles)
}