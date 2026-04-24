package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.BoardPlacementError

data class Board(
    val tiles: List<BoardTile> = listOf(BoardTile(BoardPosition(0,0), Tile(Direction.entries)))
) {

    fun Board.checkBlocked(position: BoardPosition, tile: Tile): Pair<List<BoardTile>, BoardTile> {

        val adjTiles = Direction.entries
            .mapNotNull { direction ->
                val neighbourPosition = position.neighbour(direction)
                val boardTile = tiles.find { it.pos == neighbourPosition }

                if(boardTile != null) direction to boardTile
                else null
            }

        val blocked = adjTiles
            .mapNotNull{
                if(!tile.canConnectTo(it.first, it.second.tile)) it.first
                else null
            }

        if(adjTiles.all{ (dir,_) -> blocked.contains(dir) })
            throw BoardPlacementError.TileConnectionMismatch()

        val boardTile = BoardTile(
            pos = position,
            tile = Tile(tile.connections, blocked)
        )

        return Pair(
            first = adjTiles.map{ it.second },
            second = boardTile
        )
    }

    fun place(position: BoardPosition, tile: Tile): Board {

        if(tiles.any{ it.pos == position })
            throw BoardPlacementError.PositionTaken(position.x, position.y)

        val (adjTiles, boardTile) = checkBlocked(position, tile)

        val copy = copy(tiles = tiles.plus(boardTile))

        val updatedTiles = adjTiles
            .map{ adjTile -> copy.checkBlocked(adjTile.pos, adjTile.tile).second }

        val updatedCopy = copy.tiles
            .filter{ tile -> updatedTiles.none{ it.pos == tile.pos } }
            .plus(updatedTiles)

        return copy(tiles = updatedCopy)
    }
}