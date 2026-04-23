package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.BoardPlacementError

data class Board(
    val tiles: Map<BoardPosition, Tile> = mapOf((BoardPosition(0,0) to Tile(Direction.entries)))
) {
    fun place(position: BoardPosition, tile: Tile): Board {

        if(tiles.isEmpty())
            return copy()

        if(tiles.containsKey(position))
            throw BoardPlacementError.PositionTaken(position.x, position.y)

        val adjacent =
            tile.connections
                .map{ direction -> position.neighbour(direction) }
                .mapNotNull { position -> tiles[position] }

        if(adjacent.isEmpty())
            throw BoardPlacementError.TileConnectionMismatch()

        val connections = adjacent.mapNotNull { if(it.canConnectTo(tile)) tile else null }

        if(connections.isEmpty())
            throw BoardPlacementError.TileConnectionMismatch()

        return copy(tiles = tiles + (position to tile))
    }
}