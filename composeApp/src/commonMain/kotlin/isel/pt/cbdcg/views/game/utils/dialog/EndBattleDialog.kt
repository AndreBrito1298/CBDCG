package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.BattleBet
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.views.game.utils.misc.info.ItemInfoPanel

@Composable
fun EndBattleDialog(
    player: Player?,
    isWinner: Boolean,
    bet: List<BattleBet>,
    confirm : () -> Unit,
) {
    AlertDialog(
        onDismissRequest = {  },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = if(isWinner) "You won!" else "You lost!",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if(isWinner) "Check the spoils from this battle" else "You lost an Item",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Light
                )
            }
        },
        text = {
            Box(
                modifier = Modifier.width(824.dp).height(480.dp),
                contentAlignment = Alignment.Center
            ){
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ){
                    if (isWinner) {
                        bet.filter{ it.player.user.id != player?.user?.id }.forEach { (_, item) ->
                            ItemInfoPanel(
                                item = item,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        val item = bet.find { it.player.user.id == player?.user?.id }?.item
                        if (item != null)
                            ItemInfoPanel(
                                item = item,
                                modifier = Modifier.fillMaxSize()
                            )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(onClick = { confirm() }) { Text("Confirm") }
                    }
                }
            }
        },
        confirmButton = {  }
    )
}