package isel.pt.cbdcg.views.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.views.game.utils.board.Board
import isel.pt.cbdcg.views.game.utils.InGameHeader
import isel.pt.cbdcg.views.game.utils.spectator.SpectatorPlayerSelector
import isel.pt.cbdcg.views.game.utils.board.ZoomButtons
import isel.pt.cbdcg.views.game.utils.cardInfo.CardStatsDialog

@Composable
fun SpectatorScreen(
    game: Game,
    spectator: Spectator,
){

    var selectedId by remember(game.id) { mutableStateOf<UInt?>(null) }
    var cardStats by remember { mutableStateOf<Card?>(null) }
    var zoom by remember { mutableStateOf(1f) }

    val currentPlayer = game.players.find {
        it.user.id == game.turn.playerTurn.first()
    }
    val phaseText = when (game.turn.phase) {
        TurnPhase.CONSTRUCTION -> "Construction"
        TurnPhase.SUBSTITUTION -> "Substitution"
        TurnPhase.MOVEMENT -> "Movement"
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .height(50.dp),
            contentAlignment = Alignment.CenterStart
        ){
            InGameHeader(
                modifier = Modifier.align(Alignment.CenterStart),
                dungeonTurn = game.turn.gameTurn.toString(),
                phase = phaseText,
                playerName = spectator.user.name.string,
                currentPlayerName = currentPlayer?.user?.name?.string ?: "Unknown"
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
                        canClickGrid = false,
                        canPlaceCharacter = false,
                        canEquipItem = false,
                        tileSize = 128.dp * zoom,
                        placeCard = {},
                        seeStats = { card ->
                            cardStats = card
                        }
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
                    SpectatorPlayerSelector(
                        players = game.players,
                        selectedPlayer = game.players.find { it.user.id == selectedId },
                        onSelectPlayer = { playerId ->
                            selectedId =
                                if (selectedId == playerId) null
                                else playerId
                        },
                        onSeeStats = { card ->
                            cardStats = card
                        },
                    )
                }
            }
        }
    }

    cardStats?.let { card ->
        CardStatsDialog(
            card = card,
            onDismiss = { cardStats = null }
        )
    }
}