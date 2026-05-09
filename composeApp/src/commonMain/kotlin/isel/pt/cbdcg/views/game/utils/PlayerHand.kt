package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Tile

@Composable
fun PlayerHand(
    hand: Map<UInt, Tile>,
    selectTile: (UInt, Tile) -> Unit,
    selected: UInt?,
    placeSignal: () -> Unit,
    rotateLeft: (UInt) -> Unit,
    rotateRight: (UInt) -> Unit,
){

    if (hand.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Sem cartas na mão")
        }
        return
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        hand.forEach { (index, tile) ->
            PlayerHandTile(
                tile = tile,
                index = index,
                select = { selectTile(index, tile) },
                isSelected = if(selected == null) false else selected == index,
                place = placeSignal,
                rotateLeft = { rotateLeft(index) },
                rotateRight = { rotateRight(index) }
            )
        }
    }
}