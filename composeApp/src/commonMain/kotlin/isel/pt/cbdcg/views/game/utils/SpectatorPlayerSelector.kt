package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Player

@Composable
fun SpectatorPlayerSelector(
    players: List<Player>,
    selectedPlayer: Player?,
    onSelectPlayer: (UInt) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(selectedPlayer == null){
                players.forEach { player ->
                    SpectatorPlayerBox(
                        player = player,
                        isSelected = false,
                        onClick = { onSelectPlayer(player.user) }
                    )
                }
            }
            else{
                SpectatorPlayerBox(
                    player = selectedPlayer,
                    isSelected = true,
                    onClick = { onSelectPlayer(selectedPlayer.user) }
                )

                selectedPlayer.hand.forEach { (index, tile) ->
                    SpectatorHandTile(
                        tile = tile,
                        index = index
                    )
                }
            }
        }
    }
}