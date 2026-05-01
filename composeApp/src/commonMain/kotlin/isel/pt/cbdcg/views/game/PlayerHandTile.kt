package isel.pt.cbdcg.views.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.Tile
import org.jetbrains.compose.resources.painterResource

@Composable
fun PlayerHandTile(
    tile: Tile,
    index: Int,
    select: () -> Unit,
) {
    val fileName = tile.codeString()
    val resource = Res.allDrawableResources[fileName]
        ?: error("Drawable not found: $fileName")

    Image(
        painter = painterResource(resource),
        contentDescription = "Carta ${index + 1}",
        modifier = Modifier.size(128.dp).clickable(onClick = select)
    )
}