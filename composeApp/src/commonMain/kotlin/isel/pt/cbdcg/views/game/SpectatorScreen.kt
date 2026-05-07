package isel.pt.cbdcg.views.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Game
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

@Composable
fun SpectatorScreen(
    game: Game,
){

    var selectedId by remember(game.id) { mutableStateOf<UInt?>(null) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(32.dp),
            contentAlignment = Alignment.CenterStart
        ){
            Text(
                text = "Dungeon Turn: ${game.turn.gameTurn}",
                style = MaterialTheme.typography.titleSmall
            )
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ){
            Column(
                modifier = Modifier.fillMaxSize()
            ){

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .border(2.dp, Color.Black)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Board(
                        gameBoard = game.board.tiles,
                        seeGrid = false,
                        placeTile = {},
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    SpectatorPlayerSelector(
                        players = game.players,
                        selectedPlayer = game.players.find{ it.user == selectedId },
                        onSelectPlayer = { playerId ->
                            selectedId =
                                if (selectedId == playerId) null
                                else playerId
                        }
                    )
                }
            }
        }
    }
}