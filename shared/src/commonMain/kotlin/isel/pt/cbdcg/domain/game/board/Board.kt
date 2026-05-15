package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.error.BoardPlacementError
import isel.pt.cbdcg.error.BoardPlacementError.*
import isel.pt.cbdcg.error.GameError

typealias BoardTiles = List<BoardTile>
data class Board(
    val tiles: BoardTiles = listOf(
        BoardTile(
            BoardPosition(0,0),
            Tile(Direction.entries),
            null))) {
    fun applyBoardTileEffect(result: EffectResult<BoardTile>): Board =
        when (result) {
            is EffectResult.One -> replaceBoardTile(result.value)
            is EffectResult.Many -> result.values.fold(this) { board, updatedTile -> board.replaceBoardTile(updatedTile) }
        }

    private fun replaceBoardTile(updatedTile: BoardTile): Board {
        val newTiles = tiles.filterNot { it.pos == updatedTile.pos }.toMutableList()
        newTiles.add(updatedTile)
        return copy(tiles = newTiles)
    }

    fun checkBlocked(position: BoardPosition, tile: Tile){

        val adjTiles = tile.getAdjacent(tiles, position)
        val blocked = tile.getBlocked(adjTiles)

        if(adjTiles.all{ (dir,_) -> blocked.contains(dir) })
            throw TileConnectionMismatch()
    }

    fun place(position: BoardPosition, card: Card, phase: TurnPhase): Board {
        when(card){
            is TileCard -> {
                if(phase != TurnPhase.CONSTRUCTION)
                    throw GameError.TilePlacementRestriction()

                if (tiles.any { it.pos == position })
                    throw PositionTaken(position.x, position.y)

                checkBlocked(position, card.tile)

                return copy(tiles = tiles + BoardTile(position, card.tile, null))
            }

            is CharacterCard -> {
                if(tiles.any{ it.character == card.character })
                    throw PositionTaken(position.x, position.y)

                val newBoard = tiles.map{ boardTile ->
                    if(boardTile.pos == position){
                        boardTile.addCharacter(card.character)
                    } else boardTile
                }

                return copy(tiles = newBoard)
            }
        }

    }
}
