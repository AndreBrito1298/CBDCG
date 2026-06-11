package isel.pt.cbdcg.views.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.viewmodel.GameUI
import isel.pt.cbdcg.viewmodel.GameUIState
import isel.pt.cbdcg.views.game.utils.board.Board
import isel.pt.cbdcg.views.game.utils.InGameHeader
import isel.pt.cbdcg.views.game.utils.spectator.SpectatorPlayerSelector
import isel.pt.cbdcg.views.game.utils.board.ZoomButtons
import isel.pt.cbdcg.views.game.utils.dialog.CardStatsDialog

@Composable
fun SpectatorScreen(
    spectator: Spectator,
    game: Game,
    gameUI: GameUI,
    togglePlayerHand: (Player) -> Unit,
    toggleCardStats: (Card?, BoardTile?) -> Unit,
    zoom: (Boolean) -> Unit,
){
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
                        gameState = gameUI.state,
                        gameBoard = game.board.tiles,
                        tileSize = 128.dp * gameUI.boardZoom,
                        placeCard = {},
                        player = null,
                        inspect = { card, boardTile -> toggleCardStats(card, boardTile) },
                        moveSignal = {},
                        battleSignal = {_,_ ->},
                        moveCharacter = {},
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

                    val selected =
                        if(gameUI.state is GameUIState.InspectCard)
                            (gameUI.state.previous as? GameUIState.InspectPlayer)?.player
                        else
                            (gameUI.state as? GameUIState.InspectPlayer)?.player

                    SpectatorPlayerSelector(
                        players = game.players,
                        selected = selected,
                        select = { player -> togglePlayerHand(player) },
                        onSeeStats = { card -> toggleCardStats(card, null) }
                    )
                }
            }
        }
    }

    if(gameUI.state is GameUIState.InspectCard){
        CardStatsDialog(
            card = gameUI.state.card,
            unequip = {  },
            onDismiss = { toggleCardStats(null, null) }
        )
    }
}