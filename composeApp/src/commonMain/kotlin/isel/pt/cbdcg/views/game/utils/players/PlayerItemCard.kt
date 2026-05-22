package isel.pt.cbdcg.views.game.utils.players

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun PlayerItemCard(
    item: Item,
    select: () -> Unit,
){

    val fileName = item.name
    val resource = Res.allDrawableResources[fileName]
        ?: Res.allDrawableResources["missing_texture"]
        ?: error("Drawable not found: $fileName")

    Box(
        modifier= Modifier.border(1.dp, Color.Black).padding(8.dp)
    ){
        ZoomedImage(
            resource = resource,
            zoom = 1.0f,
            select = select,
            canSelect = true
        )
    }
}