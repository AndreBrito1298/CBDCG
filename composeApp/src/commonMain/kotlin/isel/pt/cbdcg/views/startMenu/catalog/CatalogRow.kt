package isel.pt.cbdcg.views.startMenu.catalog

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun <T> CatalogRow(
    catalogItems: List<T>,
    context: CatalogContent,
    nrOfRows: Int,
    iteration: Int,
    getDrawable: suspend (String) -> ImageBitmap,
    inspect: (Card) -> Unit
) {

    for (k in 0 until nrOfRows) {

        val catalogItemIdx = iteration * nrOfRows + k
        val catalogItem = catalogItems.getOrNull(catalogItemIdx) ?: continue

        val (card, name) = when (context) {
            CatalogContent.CHARACTERS -> {
                val character = catalogItem as Character
                character.toCard() to character.name
            }
            CatalogContent.ITEMS -> {
                val item = catalogItem as Item
                item.toCard() to item.name
            }
            CatalogContent.EVENTS -> {
                val event = catalogItem as TileEffect
                event.toCard() to event.type.name
            }
        }

        Column(
            modifier = Modifier.size(128.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            ZoomedImage(
                fileName = name,
                zoom = 1f,
                loadDrawable = { getDrawable(name) },
                modifier = Modifier
                    .size(68.dp)
                    .border(1.dp, Color.Black)
                    .clickable{ inspect(card) }
                    .padding(4.dp)
            )

            Text(text = name, fontSize = 15.sp)
        }
    }

}