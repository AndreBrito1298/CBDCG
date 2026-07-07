package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun BoardTile(
    actions: BoardTileState,
    boardTile: BoardTile,
    tileName: String,
    tileSize: Dp,
    tilePath: TilePathSegment?,
    getDrawable: suspend (String) -> ImageBitmap,
    onClick: (BoardTilePossibleActions) -> Unit,
) {

    var expanded by remember { mutableStateOf(false) }
    val effectName = boardTile.tile.specialEffect.type.name
    val characterName = boardTile.character?.name

    val isClickable = actions.placeCharacter || actions.equipItem || actions.applyMovement ||
            actions.moveCharacter || actions.inspectTileEffect || actions.inspectCharacter ||
            actions.sneakThrough || actions.battleCharacter

    Box(
        modifier = Modifier
            .size(tileSize)
            .clickable(enabled = isClickable) {
                when {
                    actions.placeCharacter || actions.equipItem -> onClick(BoardTilePossibleActions.PlaceCard)
                    actions.myCharacter && actions.applyMovement -> onClick(BoardTilePossibleActions.Idle)
                    actions.applyMovement || actions.sneakThrough -> onClick(BoardTilePossibleActions.ApplyMovement)
                    else -> expanded = true
                }
            },
        contentAlignment = Alignment.Center
    ){

        // Draw the Tile
        ZoomedImage(
            fileName = tileName,
            zoom = 1.0f,
            loadDrawable = { getDrawable(tileName) },
            modifier = Modifier.size(tileSize)
        )

        // Draw the Effect
        if(effectName != "None"){
            ZoomedImage(
                fileName = effectName,
                loadDrawable = { getDrawable(effectName) },
                zoom = 0.33f,
                modifier = Modifier.size(tileSize),
                filter =
                    boardTile.cooldown?.let {
                        if(it > 0u)
                            ColorFilter.colorMatrix(ColorMatrix().apply{ setToSaturation(0f) })
                        else null
                    }
            )
        }

        // Draw the yellow path
        if(tilePath != null) {
            MovementPathOverlay(
                segment = tilePath,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Draw the Character
        if(characterName != null){
            Box(
                modifier =
                    if(actions.myCharacter) Modifier.border(1.dp, Color.Cyan, shape = CircleShape)
                    else Modifier.border(1.dp, Color.Red, shape = CircleShape)
            ){
                ZoomedImage(
                    fileName = characterName,
                    loadDrawable = { getDrawable(characterName) },
                    zoom = 0.33f,
                    modifier = Modifier.size(tileSize),
                    filter =
                        if(actions.wasBattled)
                            ColorFilter.colorMatrix(ColorMatrix().apply{ setToSaturation(0f) })
                        else null
                )
            }
        }

        // Draw the yellow dots
        if (actions.placeCharacter || actions.sneakThrough) {
            Box(
                modifier = Modifier
                    .size(tileSize / 3)
                    .background(
                        color = Color.Yellow.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Color.Yellow.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (actions.moveCharacter) {
                DropdownMenuItem(
                    text = { Text("Move Character") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.MoveCharacter)
                    }
                )
            }
            if (actions.battleCharacter) {
                DropdownMenuItem(
                    text = { Text("Challenge") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.Challenge)
                    }
                )
            }
            if (actions.inspectTileEffect) {
                DropdownMenuItem(
                    text = { Text("Inspect Tile Effect") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.InspectTileEffect)
                    }
                )
            }
            if (actions.inspectCharacter) {
                DropdownMenuItem(
                    text = { Text("Inspect Character") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.InspectCharacter)
                    }
                )
            }
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