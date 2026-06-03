package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.dto.BoardTileDTO

data class BoardTile(
    val pos: BoardPosition,
    val tile: Tile,
    val cooldown: UInt?,
    val character: Character?
): Entity {
    override fun applyToGame(game: Game): Game {
       return game.copy(board = game.board.replaceBoardTile(this))
    }
}


fun BoardTile.directionTo(other: BoardTile): Direction? =
    when {
        other.pos.x == pos.x + 1 && other.pos.y == pos.y -> Direction.EAST
        other.pos.x == pos.x - 1 && other.pos.y == pos.y -> Direction.WEST
        other.pos.x == pos.x && other.pos.y == pos.y + 1 -> Direction.NORTH
        other.pos.x == pos.x && other.pos.y == pos.y - 1 -> Direction.SOUTH
        else -> null
    }


fun BoardTile.toBoardTileDTO(): BoardTileDTO =
    BoardTileDTO(
        pos.toString(),
        tile.toTileDTO(),
        cooldown?.toInt() ?: null,
        character?.toCharacterDTO()
    )

fun BoardTileDTO.toBoardTile(): BoardTile =
    BoardTile(
        pos = pos.toPosition(),
        tile = tile.toTile(),
        cooldown = cooldown!!.toUInt(),
        character = character?.toCharacter()
    )
