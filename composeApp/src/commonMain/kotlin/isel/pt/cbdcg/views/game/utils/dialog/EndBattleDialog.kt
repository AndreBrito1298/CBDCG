package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.BattleBet
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.views.game.utils.misc.extra.SimpleClock
import isel.pt.cbdcg.views.game.utils.misc.info.ItemInfoPanel

@Composable
fun EndBattleDialog(
    getDrawable: suspend (String) -> ImageBitmap,
    remainingSeconds: Long,
    player: Player?,
    isWinner: Boolean,
    isBattling: Boolean,
    fled: Boolean,
    bet: List<BattleBet>,
    ready: List<Player>,
    total: Int,
    confirm : () -> Unit,
) {

    val clickable = player != null && (player.user.id !in ready.map{ it.user.id })
    val mainText =
        if (isBattling && isWinner) "You won!"
        else if (fled) "You fled!"
        else if (isBattling) "You lost!"
        else "End of the Battle."
    val subText =
        if (isBattling && isWinner) "Check the spoils from this battle"
        else if (fled) "You kept all your items"
        else if (isBattling) "You lost an Item"
        else "Wait for the battling players."

    AlertDialog(
        onDismissRequest = {  },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(8f)){
                    Column{
                        Text(
                            text = mainText,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = subText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Box(modifier = Modifier.weight(2f)){
                    SimpleClock(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(50.dp)
                            .padding(4.dp),
                        remainingSeconds = remainingSeconds
                    )
                }
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
                            if(item != null){
                                ItemInfoPanel(
                                    getDrawable = { getDrawable(it) },
                                    item = item,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    } else {
                        val item = bet.find { it.player.user.id == player?.user?.id }?.item
                        if (item != null)
                            ItemInfoPanel(
                                getDrawable = { getDrawable(it) },
                                item = item,
                                modifier = Modifier.fillMaxSize()
                            )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Players Remaining: ${ready.size}/$total")
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(onClick = { confirm() }, enabled = isBattling && clickable) { Text("Confirm") }
                    }
                }
            }
        },
        confirmButton = {  }
    )
}