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
    placeTile: (Tile, UInt, BoardPosition) -> Unit,
    rotateTile: (UInt, Boolean) -> Unit
) {

    var selection by remember { mutableStateOf<TileSelection>(TileSelection.None) }

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
                    Board(
                        gameBoard = game.board.tiles,
                        seeGrid = selection is TileSelection.Placing,
                        placeTile = { pos ->
                            if(selection is TileSelection.Placing) {
                                val selected = (selection as TileSelection.Placing)
                                placeTile(selected.tile, selected.idx, pos)

                                selection = TileSelection.None
                            }
                        },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {

                    PlayerHand(
                        hand = player.hand,
                        selectTile = { idx, tile ->
                            selection = when(selection) {
                                is TileSelection.None -> TileSelection.Selected(idx, tile)
                                is TileSelection.Selected -> {
                                    if((selection as TileSelection.Selected).idx == idx)
                                        TileSelection.None
                                    else TileSelection.Selected(idx, tile)
                                }
                                is TileSelection.Placing -> TileSelection.Selected(idx, tile)
                            }
                        },
                        selected = when(selection){
                            is TileSelection.None -> null
                            is TileSelection.Placing -> null
                            is TileSelection.Selected -> (selection as TileSelection.Selected).idx
                        },
                        placeSignal = {
                            if(selection is TileSelection.Selected){
                                val (idx, tile) = (selection as TileSelection.Selected)
                                selection = TileSelection.Placing(idx, tile)
                            }
                        },
                        rotateLeft = { idx ->
                            rotateTile(idx, false)
                            val (idx, tile) = (selection as TileSelection.Selected)
                            selection = TileSelection.Selected(idx, tile.rotate(false))
                        },
                        rotateRight = {
                            idx -> rotateTile(idx, true)
                            val (idx, tile) = (selection as TileSelection.Selected)
                            selection = TileSelection.Selected(idx, tile.rotate(true))
                        }
                    )

                }
            }
        }
    }
}

sealed interface TileSelection {

    data object None : TileSelection

    data class Selected(
        val idx: UInt,
        val tile: Tile
    ) : TileSelection

    data class Placing(
        val idx: UInt,
        val tile: Tile
    ) : TileSelection

}
