package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.character.Character

data class BoardTile(
    val pos: BoardPosition,
    val tile: Tile,
    val character: Character? = null
): Entity {
    fun addCharacter(character: Character): BoardTile = copy(character = character)
    fun removeCharacter(): BoardTile = copy(character = null)
}