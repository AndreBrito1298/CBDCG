package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.painterResource

@Composable
fun ZoomedImage(
    fileName: String,
    zoom: Float,
    filter: ColorFilter? = null,
    modifier: Modifier = Modifier,
) {

    val resource = Res.allDrawableResources[fileName]
        ?: Res.allDrawableResources["missing_texture"]
        ?: error("Drawable not found: $fileName")

    Box(
        modifier = modifier
            .size(128.dp)
            .clipToBounds(),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(resource),
            contentDescription = fileName,
            colorFilter = filter,
            modifier = Modifier
                .size(128.dp)
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                }
        )
    }

}