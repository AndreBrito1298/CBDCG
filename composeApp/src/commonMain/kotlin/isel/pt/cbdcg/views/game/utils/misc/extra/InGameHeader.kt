package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InGameHeader(
    modifier: Modifier,
    dungeonTurn: String,
    phase: String,
    playerName: String,
    currentPlayerName: String,
    remainingMoves: String? = null
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Dungeon Turn: $dungeonTurn | User: $playerName",
            style = MaterialTheme.typography.titleSmall
        )

        val movesText = if(remainingMoves != null)
                            " | Remaining Moves: $remainingMoves"
                        else ""

        Text(
            text = "Current Player: $currentPlayerName | Phase: $phase" + movesText,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}