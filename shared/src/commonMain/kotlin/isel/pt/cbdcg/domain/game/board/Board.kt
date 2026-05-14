package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.error.BoardPlacementError.*
import isel.pt.cbdcg.error.GameError

typealias BoardTiles = List<BoardTile>
data class Board(
    val tiles: BoardTiles = listOf(BoardTile(BoardPosition(0,0), Tile(Direction.entries), null))
) {

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

                if(phase != TurnPhase.SUBSTITUTION)
                    throw GameError.CharacterPlacementRestriction()

                val tile = tiles.find{ it.pos == position }
                    ?: throw GameError.NoTileFound(position.x, position.y)

                if(tile.character != null)
                    throw GameError.TileOccupied()

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