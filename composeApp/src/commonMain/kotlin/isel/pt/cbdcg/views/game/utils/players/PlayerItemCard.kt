package isel.pt.cbdcg.views.game.utils.players

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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun PlayerItemCard(
    item: Item,
    select: () -> Unit,
    isSelected: Boolean,
    equip: () -> Unit,
    inspect: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
){
    Box(
        modifier= Modifier
            .border(1.dp, Color.Black)
            .padding(8.dp)
            .clickable(onClick = select)
    ){
        ZoomedImage(
            fileName = item.name,
            loadDrawable = { getDrawable(item.name) },
            modifier = Modifier.size(128.dp),
            zoom = 1.0f
        )

        DropdownMenu(
            expanded = isSelected,
            onDismissRequest = select
        ){
            DropdownMenuItem(
                text = { Text("Inspect Item") },
                onClick = inspect,
            )

            if(item.grade != Grade.KEY){
                DropdownMenuItem(
                    text = { Text("Equip") },
                    onClick = equip,
                )
            }
        }
    }
}