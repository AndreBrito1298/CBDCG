package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.dto.TileDTO

data class Tile(
    val connections: List<Direction>,
) : Entity {
    override fun toString(): String =
        connections.map { it.name[0] }.sorted().joinToString("")
}

fun Tile.canConnectTo(dir: Direction, tile: Tile): Boolean{

    if(!this.connections.contains(dir)) return false

    return tile.connections.contains(dir.opposite())
}
fun Tile.rotate(right: Boolean): Tile =
    Tile(connections.map { direction ->
        if (right) direction.rotateRight()
        else direction.rotateLeft()
    })
fun Tile.getBlocked(adjTiles: List<Pair<Direction, BoardTile>>): List<Direction> =
    adjTiles.mapNotNull{
        if(!canConnectTo(it.first, it.second.tile)) it.first
        else null
    }
fun Tile.getAdjacent(tiles: BoardTiles, targetPos: BoardPosition): List<Pair<Direction, BoardTile>> =
    connections.mapNotNull { dir ->
        val neighbourPos = targetPos.neighbour(dir)
        val neighbourTile = tiles.find { it.pos == neighbourPos }

        if(neighbourTile != null) dir to neighbourTile
        else null
    }

fun Tile.toTileDTO(): TileDTO =
    TileDTO(
        connections = connections.map { it.name[0].toString() }.toTypedArray()
    )

fun TileDTO.toTile(): Tile =
    Tile(
        connections = connections.map{ it.toDirection() }
    )
