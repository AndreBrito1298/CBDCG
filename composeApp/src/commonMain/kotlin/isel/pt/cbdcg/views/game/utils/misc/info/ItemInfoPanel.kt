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
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.views.game.utils.misc.stats.CardStatsColumn

@Composable
fun ItemInfoPanel(
    item: Item,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ){
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardBasicInfoColumn(
                modifier = Modifier.width(180.dp),
                mainText = item.name,
                zoom = 1.25f,
                subText = item.grade.name,
            )

            CardStatsColumn(
                modifier = Modifier.width(200.dp),
                stats = item.stats,
            )
        }
    }
}