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
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun PlayerItemCard(
    item: Item,
    select: () -> Unit,
    isSelected: Boolean,
    equip: () -> Unit,
    inspect: () -> Unit,
){
    Box(
        modifier= Modifier.border(1.dp, Color.Black).padding(8.dp)
    ){
        ZoomedImage(
            fileName = item.name,
            zoom = 1.0f,
            select = select,
            canSelect = true
        )

        DropdownMenu(
            expanded = isSelected,
            onDismissRequest = select
        ){
            DropdownMenuItem(
                text = { Text("See Item Stats") },
                onClick = inspect,
            )

            DropdownMenuItem(
                text = { Text("Equip") },
                onClick = equip,
            )
        }
    }
}