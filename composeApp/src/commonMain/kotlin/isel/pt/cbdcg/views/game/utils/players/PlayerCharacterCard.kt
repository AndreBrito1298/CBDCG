package isel.pt.cbdcg.views.game.utils.players

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.views.game.utils.ZoomedImage


@Composable
fun PlayerCharacterCard(
    character: PlayableCharacter,
    select: () -> Unit,
    isSelected: Boolean,
    place: () -> Unit
) {

    val fileName = character.name
    val resource = Res.allDrawableResources[fileName]
        ?: error("Drawable not found: $fileName")

    Box(
        modifier= Modifier.border(1.dp, Color.Black).padding(8.dp)
    ){

        ZoomedImage(
            resource = resource,
            zoom = 2.0f,
            select = select,
            canSelect = true
        )

        DropdownMenu(
            expanded = isSelected,
            onDismissRequest = select
        ){
            DropdownMenuItem(
                text = { Text("Place Character") },
                onClick = place
            )

        }
    }
}