package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun BoardCharacter(
    conditions: BoardTileDrawConditions,
    characterName: String,
    onClick: () -> Unit,
    inspect: () -> Unit,
    moveSignal: () -> Unit
) {

    Box{

        ZoomedImage(
            fileName = characterName,
            zoom = 1.0f,
            select = onClick,
            canSelect = true
        )

        DropdownMenu(
            expanded = conditions.characterIsSelected,
            onDismissRequest = { onClick() }
        ){
            DropdownMenuItem(
                text = { Text("See Stats") },
                onClick = inspect
            )

            if (conditions.characterCanMove) {
                DropdownMenuItem(
                    text = { Text("Move") },
                    onClick = moveSignal
                )
            }
        }
    }
}