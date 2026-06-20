package isel.pt.cbdcg.views.game.utils.misc.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Stats

@Composable
fun StatsVariation(
    modifier: Modifier = Modifier,
    stats: Stats,
    deltaStats: Stats,
) {
    Column(
        modifier = modifier.height(120.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatVariation(Modifier.weight(1f), "HP", stats.hp, deltaStats.hp,Color(0xFF00B050))
        StatVariation(Modifier.weight(1f),"DMG", stats.dmg, deltaStats.dmg, Color(0xFFD00000))
        StatVariation(Modifier.weight(1f),"DEF", stats.def, deltaStats.def, Color(0xFF0070C0))
        StatVariation(Modifier.weight(1f),"SPE", stats.spe, deltaStats.spe, Color(0xFFFFC000))
    }
}