package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.error.BoardPlacementError
import isel.pt.cbdcg.error.BoardPlacementError.*

typealias BoardTiles = List<BoardTile>
data class Board(
    val tiles: BoardTiles = listOf(BoardTile(BoardPosition(0,0), Tile(Direction.entries)))
) {

    fun checkBlocked(position: BoardPosition, tile: Tile){

        val adjTiles = tile.getAdjacent(tiles, position)
        val blocked = tile.getBlocked(adjTiles)

        if(adjTiles.all{ (dir,_) -> blocked.contains(dir) })
            throw TileConnectionMismatch()
    }

    fun place(position: BoardPosition, card: Card): Board {

        when(card){

            is TileCard -> {
                if (tiles.any { it.pos == position })
                    throw PositionTaken(position.x, position.y)

                checkBlocked(position, card.tile)

                return copy(tiles = tiles + BoardTile(position, card.tile))
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