package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.TileCard
import org.jetbrains.compose.resources.painterResource

@Composable
fun SpectatorCard(
    card: Card,
) {

    val fileName =
        when(card){
            is CharacterCard -> card.character.name
            is TileCard -> card.tile.toString()
        }

    val resource = Res.allDrawableResources[fileName]
        ?: error("Drawable not found: $fileName")

    Box(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
    ) {
        Image(
            painter = painterResource(resource),
            contentDescription = "Card",
            modifier = Modifier.size(128.dp)
        )
    }
}