package isel.pt.cbdcg.domain.game

data class Tile(
    val connections: List<Direction>,
){
    fun canConnectTo(dir: Direction, tile: Tile): Boolean{

        if(!this.connections.contains(dir)) return false

        return tile.connections.contains(dir.opposite())
    }

    fun rotate(right: Boolean): Tile =
        Tile(connections.map { direction ->
            if (right) direction.rotateRight()
            else direction.rotateLeft()
        })

    fun getBlocked(adjTiles: List<Pair<Direction, BoardTile>>): List<Direction> =
        adjTiles.mapNotNull{
                if(!canConnectTo(it.first, it.second.tile)) it.first
                else null
        }

    fun getAdjacent(tiles: BoardTiles, targetPos: BoardPosition): List<Pair<Direction, BoardTile>> =
        connections.mapNotNull { dir ->
            val neighbourPos = targetPos.neighbour(dir)
            val neighbourTile = tiles.find { it.pos == neighbourPos }

            if(neighbourTile != null) dir to neighbourTile
            else null
        }

    fun codeString(): String {

        val connections = connections.map { it.name[0] }.sorted().joinToString("")
        return connections
    }

}

fun String.decodeTile(): Tile {

    val string = split("_")
    val connections = string[0].map{ it.toDirection() }

    return Tile(connections)
}

