package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTiles
import isel.pt.cbdcg.domain.game.board.getAdjacent
import isel.pt.cbdcg.domain.game.board.getBlocked


@Composable
fun Board(
    gameBoard: BoardTiles,
    canClickGrid: Boolean,
    canPlaceCharacter: Boolean,
    canEquipItem: Boolean,
    tileSize: Dp,
    placeCard: (BoardPosition) -> Unit,
    seeStats: (Card) -> Unit,
) {

    val positions = gameBoard.map { it.pos }

    val minX = positions.minOf { it.x } - 1
    val maxX = positions.maxOf { it.x } + 1
    val minY = positions.minOf { it.y } - 1
    val maxY = positions.maxOf { it.y } + 1

    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
    ) {

        for (y in maxY downTo minY) {
            Row{
                for (x in minX..maxX) {

                    val position = BoardPosition(x,y)
                    val boardTile = gameBoard.find { it.pos == position }

                    if(boardTile != null){

                        val adjTiles = boardTile.tile.getAdjacent(gameBoard, boardTile.pos)
                        val blocked = boardTile.tile.getBlocked(adjTiles)

                        val tileCode = boardTile.tile.toString() +
                            if(blocked.isNotEmpty()) "_" + blocked.map{ it.name[0] }.joinToString("")
                            else ""

                        val character = boardTile.character

                        BoardTile(
                            tileCode = tileCode,
                            characterName = character?.name,
                            position = position,
                            tileSize = tileSize,
                            placeCharacterFlag = canPlaceCharacter,
                            placeCard = { placeCard(position) },
                            equipItemFlag = canEquipItem,
                            seeStats = { if(character != null) seeStats(CharacterCard(character)) }
                        )
                    }
                    else EmptyBoardTile(canClickGrid, tileSize){ placeCard(position) }
                }
            }
        }

    }

}