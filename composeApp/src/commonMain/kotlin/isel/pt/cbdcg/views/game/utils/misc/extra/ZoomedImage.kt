package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.missing_texture
import org.jetbrains.compose.resources.imageResource

@Composable
fun ZoomedImage(
    fileName: String,
    zoom: Float,
    loadDrawable: suspend () -> ImageBitmap,
    filter: ColorFilter? = null,
    modifier: Modifier,
    contentDescription: String? = fileName,
) {
    var image by remember { mutableStateOf<ImageBitmap?>(null) }

    val fallback = imageResource(Res.drawable.missing_texture)

    LaunchedEffect(fileName) {
        runCatching {
            loadDrawable()
        }.onSuccess {
            image = it
        }
    }

    Box(
        modifier = modifier.clipToBounds(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            bitmap = image ?: fallback,
            contentDescription = contentDescription,
            colorFilter = filter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = zoom
                    scaleY = zoom
                }
        )
    }
}