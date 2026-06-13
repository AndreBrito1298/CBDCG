package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.cardInfo.CardBasicInfoColumn
import isel.pt.cbdcg.views.game.utils.cardInfo.CardStatsColumn

enum class BattleAction {
    HOLD, FLEE, ATTACK
}

@Composable
fun BattleDialog(
    battle: Battle,
    playerCharacterName: String?,
    attackTarget: () -> Unit,
    onClick: (BattleAction) -> Unit,
    onDismiss: () -> Unit
) {
    val isParticipating = battle.characters.any { it.name == playerCharacterName }
    val isMyTurn = playerCharacterName == battle.currentTurn.name

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Battle - Turn ${battle.turn}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Box(
                modifier = Modifier.width(824.dp).height(360.dp),
                contentAlignment = Alignment.Center
            ){

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ){
                    BattleInfo(
                        Modifier.fillMaxWidth().height(300.dp),
                        battle.characters,
                        battle.currentTurn
                    )

                    if (isParticipating) {
                        Row(
                            modifier = Modifier.fillMaxWidth().height(60.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            Button(onClick = { attackTarget() }, enabled = isMyTurn) { Text("Attack") }
                            Button(onClick = { onClick(BattleAction.HOLD) }, enabled = isMyTurn) { Text("Hold") }
                            Button(onClick = { onClick(BattleAction.FLEE) }, enabled = isMyTurn) { Text("Flee") }
                        }
                    }
                }
            }
        },
        confirmButton = {  }
    )
}

@Composable
fun BattleInfo(modifier: Modifier = Modifier, characters: List<Character>, attackingCharacter: Character, target: ((Character) -> Unit)? = null) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        characters.forEach { character ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardBasicInfoColumn(
                    modifier = Modifier.height(140.dp).clickable(target != null){ if(target != null) target(character) },
                    mainText = character.name,
                    zoom = 2.75f,
                    subText = if (character.name == attackingCharacter.name) "ACTIVE" else ""
                )

                CardStatsColumn(
                    modifier = Modifier.height(144.dp),
                    stats = character.adjustStats()
                )
            }
        }


    }
}