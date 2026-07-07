package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Entity
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.getAdjacent
import isel.pt.cbdcg.domain.game.board.tile.getBlocked
import isel.pt.cbdcg.domain.game.board.tile.toTile
import isel.pt.cbdcg.domain.game.board.tile.toTileDTO
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.dto.BoardTileDTO
import isel.pt.cbdcg.dto.EntityDTO

data class BoardTile(
    val pos: BoardPosition,
    val tile: Tile,
    val cooldown: UInt?,
    val character: Character?
): Entity {
    override fun Entity.toEntityDTO() = EntityDTO(boardTile = toBoardTileDTO())

    override fun <T : Entity> toEntity() = this as Entity

}
fun BoardTile.getTileName(board: BoardTiles): String {
    val adjTiles = tile.getAdjacent(board, pos)
    val blocked = tile.getBlocked(adjTiles)
    return tile.toString() +
            if(blocked.isNotEmpty()) "_" + blocked.map{ it.name[0] }.joinToString("")
            else ""
}
fun BoardTile.directionTo(other: BoardTile): Direction? =
    when (other.pos.x) {
        pos.x + 1 if other.pos.y == pos.y -> Direction.EAST
        pos.x - 1 if other.pos.y == pos.y -> Direction.WEST
        pos.x if other.pos.y == pos.y + 1 -> Direction.NORTH
        pos.x if other.pos.y == pos.y - 1 -> Direction.SOUTH
        else -> null
    }


fun BoardTile.toBoardTileDTO(): BoardTileDTO =
    BoardTileDTO(
        pos.toString(),
        tile.toTileDTO(),
        cooldown?.toInt(),
        character?.toCharacterDTO()
    )

fun BoardTileDTO.toBoardTile(): BoardTile =
    BoardTile(
        pos = pos.toPosition(),
        tile = tile.toTile(),
        cooldown = cooldown!!.toUInt(),
        character = character?.toCharacter()
    )
