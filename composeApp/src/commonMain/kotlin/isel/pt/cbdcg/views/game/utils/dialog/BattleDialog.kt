package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.cardInfo.CardBasicInfoColumn
import isel.pt.cbdcg.views.game.utils.cardInfo.CardStatsColumn

enum class BattleAction {
    ATTACK, HOLD, FLEE
}

@Composable
fun BattleDialog(
    battle: Battle,
    playerCharacterName: String?,
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
        text = { BattleInfo(battle) },
        confirmButton = {
            if (isParticipating) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    Button(
                        onClick = { onClick(BattleAction.ATTACK) },
                        enabled = isMyTurn
                    ) {
                        Text("Attack")
                    }
                    Button(
                        onClick = { onClick(BattleAction.HOLD) },
                        enabled = isMyTurn
                    ) {
                        Text("Hold")
                    }
                    Button(
                        onClick = { onClick(BattleAction.FLEE) },
                        enabled = isMyTurn
                    ) {
                        Text("Flee")
                    }
                }
            }
        }
    )
}

@Composable
fun BattleInfo(battle: Battle) {
    Box(modifier = Modifier.width(824.dp).height(360.dp)) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            battle.characters.forEach { character ->
                Column(
                    modifier = Modifier
                        .width(200.dp)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardBasicInfoColumn(
                        modifier = Modifier.height(180.dp),
                        mainText = character.name,
                        zoom = 3f,
                        subText = if (character.name == battle.currentTurn.name) "ACTIVE" else ""
                    )

                    CardStatsColumn(
                        modifier = Modifier.height(164.dp),
                        stats = character.adjustStats()
                    )
                }
            }
        }
    }
}