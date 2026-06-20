package isel.pt.cbdcg.views.game.utils.misc.info

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.misc.stats.CardStatsColumn

@Composable
fun BattleInfo(
    modifier: Modifier = Modifier,
    playerCharacterName: String? = null,
    characters: List<Character>,
    targetCharacter: Character?,
    target: ((Character) -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        characters.filter{ it.adjustStats().hp > 0 }.forEach { character ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CardBasicInfoColumn(
                    modifier = Modifier
                        .height(180.dp)
                        .clickable(target != null){ if(target != null) target(character) }
                        .then(other =
                            if(targetCharacter != null && targetCharacter.name == character.name)
                                Modifier.border(2.dp, Color.Red)
                            else Modifier
                        ),
                    mainText = character.name,
                    zoom = 2.75f,
                    subText = if(playerCharacterName != null && playerCharacterName == character.name) "YOU" else ""
                )
                CardStatsColumn(
                    modifier = Modifier.height(152.dp),
                    stats = character.adjustStats()
                )
            }
        }
    }
}