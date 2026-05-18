package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.dto.BoardTileDTO

data class BoardTile(
    val pos: BoardPosition,
    val tile: Tile,
    val character: Character?
): Entity
fun BoardTile.removeCharacter(): BoardTile = copy(character = null)
fun BoardTile.addCharacter(character: Character): BoardTile = copy(character = character)


fun BoardTile.toBoardTileDTO(): BoardTileDTO =
    BoardTileDTO(pos.coords(), tile.toTileDTO(), character?.toCharacterDTO())

fun BoardTileDTO.toBoardTile(): BoardTile =
    BoardTile(
        pos = pos.toPosition(),
        tile = tile.toTile(),
        character = character?.toCharacter()
    )
