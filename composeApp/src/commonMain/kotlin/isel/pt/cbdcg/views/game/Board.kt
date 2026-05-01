package isel.pt.cbdcg.views.game

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.BoardTiles


@Composable
fun Board(
    gameBoard: BoardTiles,
    seeGrid: Boolean,
    placeTile: (BoardPosition) -> Unit
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

                        val tileCode = boardTile.tile.codeString() +
                            if(blocked.isNotEmpty()) "_" + blocked.map{ it.name[0] }.joinToString("")
                            else ""

                        BoardTile(tileCode, position)
                    }
                    else EmptyBoardTile(seeGrid){ placeTile(BoardPosition(x,y)) }
                }
            }
        }

    }

}