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
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.views.game.utils.misc.info.BattleInfo

@Composable
fun StartBattleDialog(
    battle: Battle,
    myCharacter: Character,
    confirm: (Boolean) -> Unit,
) {

    val canClick = battle.pending.none{ it.origin.name == myCharacter.name }

    AlertDialog(
        onDismissRequest = {  },
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Battle Preparation",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
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

                    BattleInfo(
                        modifier = Modifier.fillMaxWidth().height(348.dp),
                        characters = battle.characters,
                        targetCharacter = null
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().height(24.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("Characters Ready: ${battle.pending.size}/${battle.characters.size}")
                    }


                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ) {
                        Button(onClick = { confirm(true) }, enabled = canClick) { Text("Battle") }
                        Button(onClick = { confirm(false) }, enabled = canClick) { Text("Ignore") }
                    }
                }
            }
        },
        confirmButton = {  }
    )
}