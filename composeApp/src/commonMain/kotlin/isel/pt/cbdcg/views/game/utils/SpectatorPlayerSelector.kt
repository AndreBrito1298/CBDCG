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
                    SpectatorPlayerIcons(
                        characterName = player.currentCharacter,
                        isSelected = false,
                        onClick = { onSelectPlayer(player.user.id) }
                    )
                }
            }
            else{
                SpectatorPlayerIcons(
                    characterName = selectedPlayer.currentCharacter,
                    isSelected = true,
                    onClick = { onSelectPlayer(selectedPlayer.user.id) }
                )

                selectedPlayer.hand.forEach { (_, card) ->
                    SpectatorCard(
                        card = card,
                    )
                }
            }
        }
    }
}