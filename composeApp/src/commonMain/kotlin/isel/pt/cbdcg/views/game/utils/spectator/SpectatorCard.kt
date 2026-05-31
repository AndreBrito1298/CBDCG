package isel.pt.cbdcg.views.game.utils.spectator

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun SpectatorCard(
    card: Card,
    onSeeStats: () -> Unit,
) {

    val (fileName, zoom) =
        when(card){
            is CharacterCard -> card.character.name to 2.0f
            is TileCard -> card.tile.toString() to 1.0f
            is ItemCard -> card.item.name to 1.0f
        }

    Box(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
    ) {
        when (card) {
            is CharacterCard, is ItemCard -> {
                ZoomedImage(
                    fileName = fileName,
                    zoom = zoom,
                    select = onSeeStats,
                    canSelect = true
                )
            }

            is TileCard -> {
                ZoomedImage(
                    fileName = card.tile.toString(),
                    zoom = zoom,
                    select = {},
                    canSelect = false
                )

                if(card.tile.specialEffect.type.name != "None"){
                    ZoomedImage(
                        fileName = card.tile.specialEffect.type.name,
                        zoom = 0.25f,
                        select = {},
                        canSelect = false
                    )
                }
            }
        }
    }
}