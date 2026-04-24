package isel.pt.cbdcg.views.game

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.BoardTile
import isel.pt.cbdcg.domain.game.Game

@Composable
fun GameScreen(
    game: Game,
    placePiece: (BoardPosition) -> Unit,
) {

    val tiles = game.board.tiles
    val positions = tiles.map { it.pos }

    val minX = positions.minOf { it.x } - 1
    val maxX = positions.maxOf { it.x } + 1
    val minY = positions.minOf { it.y } - 1
    val maxY = positions.maxOf { it.y } + 1

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){

        Text(
            text = "Turn: ${game.turn}",
            style = MaterialTheme.typography.titleMedium
        )

        Column(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .verticalScroll(rememberScrollState()),
        ){
            for (y in maxY downTo minY) {
                Row{
                    for (x in minX..maxX) {
                        val tile = tiles.find { it.pos.x == x && it.pos.y == y }
                        if(tile != null){ BoardTile(tile) }
                        else EmptyBoardTile{ placePiece(BoardPosition(x,y)) }
                    }
                }
            }
        }
    }
}