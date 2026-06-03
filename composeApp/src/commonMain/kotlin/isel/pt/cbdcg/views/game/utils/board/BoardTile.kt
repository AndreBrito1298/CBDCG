package isel.pt.cbdcg.views.game.utils.board

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
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun BoardTile(
    conditions: BoardTileDrawConditions,
    boardTile: BoardTile,
    tileName: String,
    tileSize: Dp,
    tilePath: TilePathSegment?,
    onClick: () -> Unit,
    selectCharacter: () -> Unit,
    inspectCharacter: () -> Unit,
    moveSignal: () -> Unit,
) {

    val effectName = boardTile.tile.specialEffect.type.name
    val characterName = boardTile.character?.name

    Box(
        modifier = Modifier
            .size(tileSize),
        contentAlignment = Alignment.Center
    ){
        ZoomedImage(
            fileName = tileName,
            zoom = 1.0f,
            select = onClick,
            canSelect = conditions.placingCharacter || conditions.characterIsMoving
        )

        if(effectName != "None"){
            ZoomedImage(
                fileName = effectName,
                zoom = 0.33f,
                select = onClick,
                canSelect = conditions.placingCharacter || conditions.characterIsMoving,
                filter =
                    boardTile.cooldown?.let {
                        if(it > 0u)
                            ColorFilter.colorMatrix(ColorMatrix().apply{ setToSaturation(0f) })
                        else null
                    }
            )
        }

        if(tilePath != null) {
            MovementPathOverlay(
                segment = tilePath,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (conditions.placingCharacter) {
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
            BoardCharacter(
                conditions = conditions,
                characterName = characterName,
                onClick = { if(conditions.equippingItem || conditions.characterIsMoving) onClick() else selectCharacter()  },
                inspect = inspectCharacter,
                moveSignal = moveSignal,
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