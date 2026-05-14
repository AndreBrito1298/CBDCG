package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import org.jetbrains.compose.resources.painterResource


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

        Image(
            painter = painterResource(resource),
            contentDescription = "Card",
            modifier = Modifier.size(128.dp).clickable(onClick = select)
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