package isel.pt.cbdcg.views.startMenu

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
import isel.pt.cbdcg.domain.game.board.tile.AllTileEffects
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun CatalogOption(
    option: CatalogContent,
    selected: CatalogContent?,
    select: (CatalogContent) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap
){

    val contentImageName = when (option) {
        CatalogContent.CHARACTERS -> PlayableCharacterCatalog.basicCharacters.first().name
        CatalogContent.ITEMS -> ItemCatalog.commonItems.first().name
        CatalogContent.EVENTS -> AllTileEffects.allTileEffects.keys.first().type.name
    }
    val borderColor = if(selected == option) Color.Green else Color.Black

    Column(
        modifier = Modifier.size(128.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ZoomedImage(
            fileName = contentImageName,
            zoom = 1f,
            loadDrawable = { getDrawable(contentImageName) },
            modifier = Modifier
                .size(68.dp)
                .border(1.dp, borderColor)
                .clickable { select(option) }
                .padding(4.dp)
        )

        Text(text = option.name, fontSize = 15.sp)
    }

}