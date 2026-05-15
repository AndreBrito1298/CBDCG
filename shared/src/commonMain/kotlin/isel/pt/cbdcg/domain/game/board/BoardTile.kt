package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.dto.BoardTileDTO

data class BoardTile(
    val pos: BoardPosition,
    val tile: Tile,
    val character: Character?
) {

    fun addCharacter(character: Character): BoardTile = copy(character = character)

    fun toBoardTileDTO(): BoardTileDTO =
        BoardTileDTO(pos.coords(), tile.toString(), character?.string ?: "")

}