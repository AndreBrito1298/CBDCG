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

fun Board.connectedNeighbours(boardTile: BoardTile): List<BoardTile> =
    boardTile.tile.connections.mapNotNull { dir ->
        val nextPos = boardTile.pos.neighbour(dir)
        val nextTile = tiles.find { it.pos == nextPos } ?: return@mapNotNull null

        if (boardTile.tile.canConnectTo(dir, nextTile.tile)) nextTile
        else null
    }

fun Board.connectionDistancesFrom(target: BoardTile): Map<BoardTile, Int> {
    val distances = mutableMapOf<BoardTile, Int>()
    val queue = ArrayDeque<BoardTile>()

    distances[target] = 0
    queue.add(target)

    while (queue.isNotEmpty()) {
        val current = queue.removeFirst()
        val currentDistance = distances.getValue(current)

        for (next in connectedNeighbours(current)) {
            if (next in distances) continue

            distances[next] = currentDistance + 1
            queue.add(next)
        }
    }

    return distances
}

// Algorítmo BFS - o algoritmo usado pode ser mudado no futuro
fun Board.findPath(from: BoardTile, to: BoardTile, maxDistance: Int): List<BoardTile> {
    val distancesToTarget = connectionDistancesFrom(to)

    val queue = ArrayDeque(listOf(listOf(from)))
    val visited = mutableSetOf(from)

    var bestPath = listOf(from)

    while (queue.isNotEmpty()) {
        val path = queue.removeFirst()
        val current = path.last()

        if (current == to) return path

        val currentDistance = distancesToTarget[current]
        val bestDistance = distancesToTarget[bestPath.last()]

        if (
            currentDistance != null &&
            (bestDistance == null || currentDistance < bestDistance)
        ) {
            bestPath = path
        }

        if (path.size - 1 >= maxDistance) continue
        if (current != from && current.character != null) continue

        for (next in connectedNeighbours(current)) {
            if (next in visited) continue

            visited.add(next)
            queue.add(path + next)
        }
    }

    return bestPath
}


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
        if(boardTile.pos == position)boardTile.copy(character = character)
        else boardTile
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