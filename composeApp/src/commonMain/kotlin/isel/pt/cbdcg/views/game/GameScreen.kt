package isel.pt.cbdcg.views.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Tile

@Composable
fun GameScreen(
    player: Player,
    game: Game,
    placeTile: (Tile, BoardPosition) -> Unit,
) {

    var tileSelected by remember { mutableStateOf<Tile?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ){

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(32.dp),
            contentAlignment = Alignment.CenterStart
        ) {

            val myTurn = game.turn.playerTurn.first() == player.user
            val turnText =  if(myTurn) "It is your turn to play."
                            else "Wait, it is not your turn yet."

            Text(
                text = "Dungeon Turn: ${game.turn.gameTurn} [ $turnText ]",
                style = MaterialTheme.typography.titleSmall
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {

            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .fillMaxWidth()
                ) {
                    Board(game.board.tiles, tileSelected != null) { pos ->
                        val tile = tileSelected
                        if(tile != null) {
                            placeTile(tile, pos)
                            tileSelected = null
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PlayerHand(player.hand) { tile ->
                        tileSelected =  if(tile == tileSelected) null
                                        else tile
                    }
                }
            }
        }
    }
}