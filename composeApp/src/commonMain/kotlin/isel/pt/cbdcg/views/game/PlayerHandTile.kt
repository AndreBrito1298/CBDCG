package isel.pt.cbdcg.views.game

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
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
    index: UInt,
    select: () -> Unit,
    isSelected: Boolean,
    place: () -> Unit,
    rotateLeft: () -> Unit,
    rotateRight: () -> Unit,
) {
    val fileName = tile.codeString()
    val resource = Res.allDrawableResources[fileName]
        ?: error("Drawable not found: $fileName")

    Box{

        Image(
            painter = painterResource(resource),
            contentDescription = "Carta ${index + 1u}",
            modifier = Modifier.size(128.dp).clickable(onClick = select)
        )

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