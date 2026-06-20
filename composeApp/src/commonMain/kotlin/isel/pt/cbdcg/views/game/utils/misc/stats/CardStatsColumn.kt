package isel.pt.cbdcg.views.game.utils.misc.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Stats

@Composable
fun CardStatsColumn(
    modifier: Modifier,
    stats: Stats,
){
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatRow("HP", stats.hp, Color(0xFF00B050))
        StatRow("DMG", stats.dmg, Color(0xFFD00000))
        StatRow("DEF", stats.def, Color(0xFF0070C0))
        StatRow("SPE", stats.spe, Color(0xFFFFC000))
    }
}

