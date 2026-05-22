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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CardType
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.rotate
import isel.pt.cbdcg.views.game.CardSelection.*
import isel.pt.cbdcg.views.game.utils.board.Board
import isel.pt.cbdcg.views.game.utils.InGameHeader
import isel.pt.cbdcg.views.game.utils.players.PlayerHand
import isel.pt.cbdcg.views.game.utils.board.ZoomButtons

@Composable
fun GameScreen(
    player: Player,
    game: Game,
    placeOnBoard: (Card, UInt, BoardPosition) -> Unit,
    rotateTile: (UInt, Boolean) -> Unit,
    nextPhase: () -> Unit
) {

    var selection by remember { mutableStateOf<CardSelection>(None) }
    var zoom by remember { mutableStateOf(1f) }

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

                    val cardtype = if (selection is PlaceCard) { (selection as PlaceCard).card.type }
                                   else null

                    Board(
                        gameBoard = game.board.tiles,
                        canClickGrid = cardtype != null && cardtype == CardType.TILE,
                        canClickTiles = cardtype != null && cardtype == CardType.CHARACTER,
                        tileSize = 128.dp * zoom,
                        placeCard = { pos ->
                            if (selection is PlaceCard) {
                                val (idx, card) = (selection as PlaceCard)
                                placeOnBoard(card, idx ,pos)

                                selection = None
                            }
                        },
                    )
                    ZoomButtons(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        amplify = { zoom = (zoom + 0.25f).coerceAtMost(2f) },
                        reduce = { zoom = (zoom - 0.25f).coerceAtLeast(0.5f) },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PlayerHand(
                        hand = player.hand,
                        selectCard = { idx, card ->
                            selection = when (selection) {
                                is None, is PlaceCard -> Selected(idx, card)
                                is Selected -> {
                                    if ((selection as Selected).idx == idx)
                                        None
                                    else Selected(idx, card)
                                }
                            }
                        },
                        selected = when (selection) {
                            is None -> null
                            is PlaceCard -> null
                            is Selected -> (selection as Selected).idx
                        },
                        placeSignal = {
                            if(selection is Selected){
                                val (idx, card) = (selection as Selected)
                                selection = PlaceCard(idx, card)
                            }
                        },
                        rotateLeft = { idx ->
                            if(selection is Selected){
                                val (selIdx, card) = (selection as Selected)
                                if(selIdx != idx) return@PlayerHand

                                selection = when(card){
                                    is TileCard -> {
                                        rotateTile(idx, false)
                                        Selected(idx, card.copy(tile = card.tile.rotate(false)))
                                    }
                                    else -> selection
                                }
                            }
                        },
                        rotateRight = { idx ->
                            if(selection is Selected){
                                val (selIdx, card) = (selection as Selected)
                                if(selIdx != idx) return@PlayerHand

                                selection = when(card){
                                    is TileCard -> {
                                        rotateTile(idx, true)
                                        Selected(idx, card.copy(tile = card.tile.rotate(true)))
                                    }
                                    else -> selection
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

sealed interface CardSelection {

    data object None : CardSelection
    data class Selected(
        val idx: UInt,
        val card: Card
    ) : CardSelection
    data class PlaceCard(
        val idx: UInt,
        val card: Card
    ) : CardSelection

}
