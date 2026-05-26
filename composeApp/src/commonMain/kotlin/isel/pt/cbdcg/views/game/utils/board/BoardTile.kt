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
import isel.pt.cbdcg.domain.game.CardType
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.viewmodel.GameUIState
import org.jetbrains.compose.resources.painterResource

@Composable
fun BoardTile(
    gameState: GameUIState,
    tileName: String,
    characterName: String?,
    position: BoardPosition,
    tileSize: Dp,
    onClick: () -> Unit,
) {

    val tileResource = Res.allDrawableResources[tileName]
        ?: error("Drawable not found: $tileName")

    val cardType = (gameState as? GameUIState.PlacingCard)?.card?.type
    val canPlaceTile = (gameState as? GameUIState.PlacingCard)?.card?.type == CardType.TILE
    val canPlaceCharacter = (gameState as? GameUIState.PlacingCard)?.card?.type == CardType.CHARACTER

    Box(
        modifier = Modifier
            .size(tileSize)
            .then(
                if(canPlaceTile || canPlaceCharacter)
                    Modifier.clickable{ onClick() }
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(tileResource),
            contentDescription = "Tile ${position.x}, ${position.y}",
            modifier = Modifier.fillMaxSize()
        )

        if (cardType != null && cardType == CardType.CHARACTER) {
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
                modifier = Modifier
                    .size(tileSize)
                    .clickable{ onClick() }
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