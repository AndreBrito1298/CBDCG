package isel.pt.cbdcg.views.game.utils.misc.layout

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.misc.extra.InGameHeader

@Composable
fun GameScreenHeader(
    game: Game,
    currentPlayer: Player?,
    thisPlayer: Player?,
    movementUsed: Int,
    nextPhase: () -> Unit,
    leaveGame: () -> Unit,
){

    val (phaseText, nextPhaseText) = when(game.turn.phase){
        TurnPhase.CONSTRUCTION -> "Construction" to "Next: Substitution"
        TurnPhase.SUBSTITUTION -> "Substitution" to "Next: Movement"
        TurnPhase.MOVEMENT -> "Movement" to "End Turn"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(50.dp),
        contentAlignment = Alignment.CenterStart
    ) {

        val playing = currentPlayer != null && currentPlayer.user.id == thisPlayer?.user?.id
        val playingCharacter =
            if(playing) game.board.tiles.firstOrNull{ it.character?.name == thisPlayer.currentCharacter }?.character
            else null

        InGameHeader(
            modifier = Modifier.align(Alignment.CenterStart),
            dungeonTurn = game.turn.gameTurn.toString(),
            phase = phaseText,
            playerName = thisPlayer?.user?.name?.string,
            currentPlayerName = currentPlayer?.user?.name?.string ?: "Unknown",
            remainingMoves =
                if(playingCharacter!= null) "${(playingCharacter.adjustStats().spe - movementUsed).coerceAtLeast(0)}"
                else null
        )

        if (playing && game.turn.gameTurn > 0u) {
            Button(
                onClick = nextPhase,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(nextPhaseText)
            }
        } else {
            Button(
                onClick = leaveGame,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text("Leave Game")
            }
        }
    }
}