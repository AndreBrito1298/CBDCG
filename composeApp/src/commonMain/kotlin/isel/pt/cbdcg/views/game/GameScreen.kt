package isel.pt.cbdcg.views.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.viewmodel.GameUI
import isel.pt.cbdcg.viewmodel.GameUIState
import isel.pt.cbdcg.views.game.utils.board.Board
import isel.pt.cbdcg.views.game.utils.InGameHeader
import isel.pt.cbdcg.views.game.utils.players.PlayerHand
import isel.pt.cbdcg.views.game.utils.board.ZoomButtons
import isel.pt.cbdcg.views.game.utils.cardInfo.CardStatsDialog

@Composable
fun GameScreen(
    player: Player,
    game: Game,
    gameUI: GameUI,
    selectCard: (UInt, Card) -> Unit,
    placeSignal: () -> Unit,
    placeOnBoard: (BoardPosition) -> Unit,
    toggleCardStats: (Card?) -> Unit,
    rotateTile: (Boolean) -> Unit,
    zoom: (Boolean) -> Unit,
    nextPhase: () -> Unit
) {

    val currentPlayer = game.players.find {
        it.user.id == game.turn.playerTurn.first()
    }
    val phaseText = when (game.turn.phase) {
        TurnPhase.CONSTRUCTION -> "Construction"
        TurnPhase.SUBSTITUTION -> "Substitution"
        TurnPhase.MOVEMENT -> "Movement"
    }
    val nextPhaseText = when (game.turn.phase) {
        TurnPhase.CONSTRUCTION -> "Next: Substitution"
        TurnPhase.SUBSTITUTION -> "Next: Movement"
        TurnPhase.MOVEMENT -> "End Turn"
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ){

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            InGameHeader(
                modifier = Modifier.align(Alignment.CenterStart),
                dungeonTurn = game.turn.gameTurn.toString(),
                phase = phaseText,
                playerName = player.user.name.string,
                currentPlayerName = currentPlayer?.user?.name?.string ?: "Unknown"
            )

            if (currentPlayer != null && currentPlayer.user.id == player.user.id && game.turn.gameTurn > 0u) {
                Button(
                    onClick = nextPhase,
                    modifier = Modifier.align(Alignment.BottomEnd)
                ) {
                    Text(nextPhaseText)
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .border(2.dp, Color.Black)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Board(
                        gameState = gameUI.state,
                        gameBoard = game.board.tiles,
                        tileSize = 128.dp * gameUI.boardZoom,
                        placeCard = { pos -> placeOnBoard(pos) },
                        seeStats = { card -> toggleCardStats(card) }
                    )
                    ZoomButtons(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        amplify = { zoom(true) },
                        reduce = { zoom(false) },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PlayerHand(
                        hand = player.hand,
                        selectCard = { idx, card -> selectCard(idx, card) },
                        selected = (gameUI.state as? GameUIState.SelectCard)?.idx,
                        placeCard = placeSignal,
                        inspectCard = { card -> toggleCardStats(card) },
                        rotateLeft = { rotateTile(false) },
                        rotateRight = { rotateTile(true) },
                    )
                }
            }
        }
    }

    if(gameUI.state is GameUIState.InspectCard){
        CardStatsDialog(
            card = gameUI.state.card,
            onDismiss = { toggleCardStats(null) }
        )
    }
}
