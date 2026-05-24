package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Character

@Composable
fun CharacterInfoPanel(
    character: Character,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            CardBasicInfoColumn(
                modifier = Modifier.width(180.dp),
                name = character.name,
                zoom = 3.0f,
                grade = character.grade.name,
            )
            CharacterItemAndConditionColumn(
                modifier = Modifier.width(180.dp),
                character = character,
            )
            CardStatsColumn(
                modifier = Modifier.width(200.dp),
                stats = character.adjustStats(),
            )
        }
    }
}