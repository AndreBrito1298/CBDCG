package isel.pt.cbdcg.views.game.utils.misc.info

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.misc.extra.CharacterEquippedItemsColumn
import isel.pt.cbdcg.views.game.utils.misc.extra.CharacterEvolutionColumn
import isel.pt.cbdcg.views.game.utils.misc.stats.CardStatsColumn

@Composable
fun CharacterInfoPanel(
    character: Character,
    getDrawable: suspend (String) -> ImageBitmap,
    unequip: (idx: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically

        ) {
            CardBasicInfoColumn(
                getDrawable = { getDrawable(it) },
                modifier = Modifier.width(160.dp),
                mainText = character.name,
                zoom = 1.0f,
                subText = character.grade.name,
            )
            CardStatsColumn(
                modifier = Modifier.width(190.dp),
                stats = character.adjustStats(),
            )
            CharacterEvolutionColumn(
                getDrawable = { getDrawable(it) },
                modifier = Modifier.width(160.dp),
                character = character,
            )
            CharacterEquippedItemsColumn(
                getDrawable = { getDrawable(it) },
                modifier = Modifier.width(160.dp),
                unequip = { idx -> unequip(idx) },
                character = character,
            )
        }
    }
}