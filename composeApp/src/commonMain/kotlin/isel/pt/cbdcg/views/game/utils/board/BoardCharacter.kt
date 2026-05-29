package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.painterResource

@Composable
fun BoardCharacter(
    conditions: BoardTileDrawConditions,
    characterName: String,
    tileSize: Dp,
    onClick: () -> Unit,
    inspect: () -> Unit,
    moveSignal: () -> Unit
) {

    val characterResource = Res.allDrawableResources[characterName]
        ?: error("Drawable not found: $characterName")

    Box{
        Image(
            painter = painterResource(characterResource),
            contentDescription = characterName,
            modifier = Modifier
                .size(tileSize)
                .clickable { onClick() }
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