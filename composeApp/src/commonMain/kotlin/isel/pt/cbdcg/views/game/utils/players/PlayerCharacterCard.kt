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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage


@Composable
fun PlayerCharacterCard(
    character: PlayableCharacter,
    select: () -> Unit,
    isSelected: Boolean,
    place: () -> Unit,
    inspect: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
) {
    Box(
        modifier= Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
            .clickable(onClick = select)
    ){

        ZoomedImage(
            fileName = character.name,
            loadDrawable = { getDrawable(character.name) },
            zoom = 1.0f
        )

        DropdownMenu(
            expanded = isSelected,
            onDismissRequest = select
        ){
            DropdownMenuItem(
                text = { Text("Place Character") },
                onClick = place
            )

            DropdownMenuItem(
                text = { Text("See Character Stats") },
                onClick = inspect
            )
        }
    }
}