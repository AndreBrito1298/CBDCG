package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.equipItem
import isel.pt.cbdcg.error.BoardError
import kotlin.collections.plus

typealias BoardTiles = List<BoardTile>

data class Board(
    val tiles: BoardTiles = listOf(
        BoardTile(
            pos = BoardPosition(0,0),
            tile = Tile(Direction.entries),
            character = null
        )
    )
): Entity
fun Board.checkBlocked(position: BoardPosition, tile: Tile){

    val adjTiles = tile.getAdjacent(tiles, position)
    val blocked = tile.getBlocked(adjTiles)

    if(adjTiles.all{ (dir,_) -> blocked.contains(dir) })
        throw BoardError.TileConnectionMismatch()
}
fun Board.placeTile(position: BoardPosition, tile: Tile, turnPhase: TurnPhase): Board {
    if(turnPhase != TurnPhase.CONSTRUCTION)
        throw BoardError.TilePlacementRestriction()

    if (tiles.any { it.pos == position })
        throw BoardError.PositionTaken(position.x, position.y)

    checkBlocked(position, tile)

    return copy(tiles = tiles + BoardTile(position, tile, null))
}
fun Board.placeCharacter(position: BoardPosition, player: Player, character: Character, turnPhase: TurnPhase): Board {
    if(turnPhase != TurnPhase.SUBSTITUTION)
        throw BoardError.CharacterPlacementRestriction()

    if(player.currentCharacter != null)
        throw BoardError.CharacterLimitReached()

    if(tiles.any{ it.pos == position && it.character != null })
        throw BoardError.TileOccupied()

    val newBoard = tiles.map{ boardTile ->
        if(boardTile.pos == position){
            boardTile.addCharacter(character)
        } else boardTile
    }

    return copy(tiles = newBoard)
}
fun Board.equipItem(position: BoardPosition, player: Player, item: Item, turnPhase: TurnPhase): Board{
    if(turnPhase == TurnPhase.CONSTRUCTION)
        throw BoardError.EquipItemRestriction()

    val tile = tiles.find{ it.pos == position }
        ?: throw BoardError.TileNotFound(position.x, position.y)
    val character = tile.character
        ?: throw BoardError.EmptyTile()

    if(character !is PlayableCharacter || character.name != player.currentCharacter)
        throw BoardError.EquipYourCharacter()

    val newBoard = tiles.map{ boardTile ->
        if(boardTile == tile) tile.copy(character = character.equipItem(item))
        else boardTile
    }

    return copy(tiles = newBoard)
}
fun Board.applyBoardTileEffect(result: EffectResult<BoardTile>): Board =
    when (result) {
        is EffectResult.One -> replaceBoardTile(result.value)
        is EffectResult.Many -> result.values.fold(this) { board, updatedTile -> board.replaceBoardTile(updatedTile) }
    }
private fun Board.replaceBoardTile(updatedTile: BoardTile): Board {
    val newTiles = tiles.filterNot { it.pos == updatedTile.pos }.toMutableList()
    newTiles.add(updatedTile)
    return copy(tiles = newTiles)
}