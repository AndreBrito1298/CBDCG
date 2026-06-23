package isel.pt.cbdcg.views.game.utils.dialog

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.misc.info.ActionsInfo
import isel.pt.cbdcg.views.game.utils.misc.info.BattleInfo
import kotlin.collections.filter

@Composable
fun BattleDialog(
    battle: Battle,
    playerCharacterName: String?,
    attackTarget: () -> Unit,
    onClick: (PossibleBattleActions?) -> Unit,
    onDismiss: () -> Unit
) {

    var showActions by rememberSaveable { mutableStateOf(false) }
    val isParticipating = battle.characters.any { it.name == playerCharacterName }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Battle - Turn ${battle.currentTurn}",
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
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                ){

                    Row(
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                    ){
                        Button(onClick = { showActions = !showActions }) { Text("Show Battle Actions") }
                    }

                    if(!showActions){
                        BattleInfo(
                            modifier = Modifier.fillMaxWidth().height(348.dp),
                            playerCharacterName = playerCharacterName,
                            characters = battle.characters,
                            targetCharacter = null
                        )
                    } else {
                        ActionsInfo(
                            modifier = Modifier.fillMaxWidth().height(348.dp).padding(4.dp),
                            characters = battle.characters,
                            currentTurn = battle.currentTurn,
                            actions = battle.actions
                        )
                    }

                    if (isParticipating) {

                        val canClick = battle.pending.find{ it.origin.adjustStats().hp > 0 && it.origin.name == playerCharacterName } == null
                        val actionsQueued = battle.pending.size
                        val availableCharacters = battle.characters.filter{ it.adjustStats().hp > 0 }.size

                        val lastTurnActions = battle.actions.filter{ it.key == battle.currentTurn - 1u }.flatMap{ it.value }

                        val heldLastTurn = lastTurnActions.indexOfFirst{
                            it.action == PossibleBattleActions.HOLD && it.origin.name == playerCharacterName
                        }
                        val attackedLastTurn = lastTurnActions.indexOfLast{
                            it.action == PossibleBattleActions.ATTACK && it.target != null && it.target?.name == playerCharacterName
                        }
                        val fleeChanceIncreased =
                            if(attackedLastTurn < heldLastTurn) true
                            else{
                                val lastAttackModifier = battle.characters
                                    .firstOrNull{ it.name == playerCharacterName }
                                    ?.activeStatModifiers
                                    ?.lastOrNull{ it.duration == 0u && it.type == ModifierType.BATTLE_HOLD }

                                lastAttackModifier != null && lastAttackModifier.stats.hp == 0
                            }


                        Row(
                            modifier = Modifier.fillMaxWidth().height(24.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text("Characters Ready: $actionsQueued/$availableCharacters")
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth().height(40.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                        ) {
                            Button(onClick = { attackTarget() }, enabled = canClick) { Text("Attack") }
                            Button(onClick = { onClick(PossibleBattleActions.HOLD) }, enabled = canClick && heldLastTurn == -1) { Text("Hold") }
                            Button(onClick = { onClick(PossibleBattleActions.FLEE) }, enabled = canClick) {
                                Text(
                                    text = "Flee",
                                    color = if(fleeChanceIncreased) Color.Yellow else Color.White,
                                    fontWeight = if(fleeChanceIncreased) FontWeight.Bold else FontWeight.Normal,
                                )
                            }
                            Button(onClick = { onClick(null) }, enabled = !canClick) { Text("Undo") }
                        }
                    }
                }
            }
        },
        confirmButton = {  }
    )
}