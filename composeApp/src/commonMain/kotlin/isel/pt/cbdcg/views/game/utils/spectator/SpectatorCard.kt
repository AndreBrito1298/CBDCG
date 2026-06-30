package isel.pt.cbdcg.views.game.utils.spectator

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun SpectatorCard(
    card: Card,
    onSeeStats: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
) {

    val fileName =
        when(card){
            is CharacterCard -> card.character.name
            is TileCard -> card.tile.toString()
            is ItemCard -> card.item.name
        }

    Box(
        modifier = Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
            .then(
                if (card !is TileCard) Modifier.clickable { onSeeStats() }
                else Modifier
            )
    ) {
        when (card) {
            is CharacterCard, is ItemCard -> {
                ZoomedImage(
                    fileName = fileName,
                    loadDrawable = { getDrawable(fileName) },
                    zoom = 1.0f
                )
            }

            is TileCard -> {
                ZoomedImage(
                    fileName = card.tile.toString(),
                    loadDrawable = { getDrawable(card.tile.toString()) },
                    zoom = 1.0f,
                )

                if(card.tile.specialEffect.type.name != "None"){
                    ZoomedImage(
                        fileName = card.tile.specialEffect.type.name,
                        loadDrawable = { getDrawable(card.tile.specialEffect.type.name) },
                        zoom = 0.25f,
                    )
                }
            }
        }
    }
}