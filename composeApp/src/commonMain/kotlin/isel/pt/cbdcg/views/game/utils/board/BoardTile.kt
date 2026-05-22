package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.board.BoardPosition
import org.jetbrains.compose.resources.painterResource

@Composable
fun BoardTile(
    tileCode: String,
    characterName: String?,
    position: BoardPosition,
    tileSize: Dp,
    canClickTiles: Boolean,
    placeCharacter: () -> Unit,
) {

    val tileResource = Res.allDrawableResources[tileCode]
        ?: error("Drawable not found: $tileCode")

    Box(
        modifier = Modifier
            .size(tileSize)
            .then(
                if(canClickTiles)
                    Modifier.clickable { placeCharacter() }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(tileResource),
            contentDescription = "Tile ${position.x}, ${position.y}",
            modifier = Modifier.fillMaxSize()
        )

        if (canClickTiles) {
            Box(
                modifier = Modifier
                    .size(tileSize / 3)
                    .background(
                        color = Color.White.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Color.Black.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )
        }

        if(characterName != null){

            val characterResource = Res.allDrawableResources[characterName]
                ?: error("Drawable not found: $characterName")

            Image(
                painter = painterResource(characterResource),
                contentDescription = "Character",
                modifier = Modifier.size(tileSize)
            )
        }
    }
}

@Composable
fun EmptyBoardTile(
    seeGrid: Boolean,
    tileSize: Dp,
    placeTile: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(tileSize)
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