package isel.pt.cbdcg.views.game.utils

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
) {

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Dungeon Turn: $dungeonTurn | User: $playerName",
            style = MaterialTheme.typography.titleSmall
        )

        Text(
            text = "Current Player: $currentPlayerName | Phase: $phase",
            style = MaterialTheme.typography.bodyMedium
        )
    }

}