package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.BoardPlacementError

typealias BoardTiles = List<BoardTile>
data class Board(
    val tiles: BoardTiles = listOf(BoardTile(BoardPosition(0,0), Tile(Direction.entries)))
) {

    fun checkBlocked(position: BoardPosition, tile: Tile){

        val adjTiles = tile.getAdjacent(tiles, position)
        val blocked = tile.getBlocked(adjTiles)

        if(adjTiles.all{ (dir,_) -> blocked.contains(dir) })
            throw BoardPlacementError.TileConnectionMismatch()
    }

    fun place(position: BoardPosition, tile: Tile): Board {

        if(tiles.any{ it.pos == position })
            throw BoardPlacementError.PositionTaken(position.x, position.y)

        checkBlocked(position, tile)

        return copy(tiles = tiles + BoardTile(position, tile))

    }
}