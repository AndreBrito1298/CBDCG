package isel.pt.cbdcg.domain.game.board.tile

import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.BoardTiles
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.neighbour
import isel.pt.cbdcg.domain.game.board.opposite
import isel.pt.cbdcg.domain.game.board.rotateLeft
import isel.pt.cbdcg.domain.game.board.rotateRight
import isel.pt.cbdcg.domain.game.board.toDirection
import isel.pt.cbdcg.dto.TileDTO

data class Tile(
    val connections: List<Direction>,
    val specialEffect: TileEffect = TileEffect()
) {
    override fun toString(): String =
        connections.map { it.name[0] }.sorted().joinToString("")
}

fun Tile.canConnectTo(dir: Direction, tile: Tile): Boolean{

    if(!this.connections.contains(dir)) return false

    return tile.connections.contains(dir.opposite())
}
fun Tile.rotate(right: Boolean): Tile =
    this.copy(
        connections = connections.map { direction ->
            if (right) direction.rotateRight()
            else direction.rotateLeft()
        }
    )
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
fun Tile.allRotations(): List<Tile> =
    generateSequence(this) { it.rotate(right = true) }
        .take(4)
        .distinctBy { it.connections.toSet() }
        .toList()

fun Tile.toTileDTO(): TileDTO =
    TileDTO(
        connections = connections.map { it.name[0].toString() }.toTypedArray(),
        specialEffect = specialEffect.toTileEffectDTO()
    )
fun TileDTO.toTile(): Tile =
    Tile(
        connections = connections.map{ it.toDirection() },
        specialEffect = specialEffect.toTileEffect()
    )