package isel.pt.cbdcg.views.game.utils.spectator

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun SpectatorCard(
    card: Card,
) {

    val (fileName, zoom) =
        when(card){
            is CharacterCard -> card.character.name to 2.0f
            is TileCard -> card.tile.toString() to 1.0f
            is ItemCard -> card.item.name to 1.0f
        }

    val resource = Res.allDrawableResources[fileName]
        ?: Res.allDrawableResources["missing_texture"]
        ?: error("Drawable not found: $fileName")

    Box(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
    ) {
        ZoomedImage(
            resource = resource,
            zoom = zoom,
            select = {},
            canSelect = false
        )
    }
}