package isel.pt.cbdcg.views.game.utils.players

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun PlayerTileCard(
    tile: Tile,
    select: () -> Unit,
    isSelected: Boolean,
    place: () -> Unit,
    rotateLeft: () -> Unit,
    rotateRight: () -> Unit,
) {
    Box(
        modifier= Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
            .clickable(onClick = select)
    ){

        ZoomedImage(
            fileName = tile.toString(),
            zoom = 1.0f
        )

        if(tile.specialEffect.type.name != "None"){
            ZoomedImage(
                fileName = tile.specialEffect.type.name,
                zoom = 0.33f
            )
        }

        DropdownMenu(
            expanded = isSelected,
            onDismissRequest = select
        ) {
            DropdownMenuItem(
                text = { Text("Place Tile") },
                onClick = place
            )

            DropdownMenuItem(
                text = { Text("Rotate Left") },
                onClick = rotateLeft
            )

            DropdownMenuItem(
                text = { Text("Rotate Right") },
                onClick = rotateRight
            )
        }

    }
}