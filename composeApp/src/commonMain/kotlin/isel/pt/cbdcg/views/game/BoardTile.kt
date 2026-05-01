package isel.pt.cbdcg.views.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.BoardPosition
import org.jetbrains.compose.resources.painterResource

@Composable
fun BoardTile(tileCode: String, position: BoardPosition) {

    val resource = Res.allDrawableResources[tileCode]
        ?: error("Drawable not found: $tileCode")

    Box(
        modifier = Modifier
            .size(128.dp),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(resource),
            contentDescription = "Tile ${position.x}, ${position.y}",
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
fun EmptyBoardTile(
    seeGrid: Boolean,
    placeTile: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(128.dp)
            .then(
                if (seeGrid) {
                    Modifier
                        .border(1.dp, Color.Gray)
                        .clickable(onClick = placeTile)
                } else {
                    Modifier
                }
            )
    )
}