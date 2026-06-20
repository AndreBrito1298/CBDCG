package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.views.game.utils.misc.extra.AffectedCharactersColumn
import isel.pt.cbdcg.views.game.utils.misc.info.CardBasicInfoColumn

@Composable
fun TileEffectDialog(
    effect: TileEffect,
    activate: Boolean,
    onConfirm: () -> Unit,
    affectedCharacters: List<Character> = emptyList(),
){
    AlertDialog(
        onDismissRequest = onConfirm,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(if(activate) "Activate" else "Dismiss")
            }
        },
        text = {
            Box(modifier = Modifier.width(775.dp).height(340.dp)){
                TileEffectInfo(
                    modifier = Modifier.fillMaxSize(),
                    effectName = effect.type.name,
                    description = effect.info,
                    affectedCharacters = affectedCharacters
                )
            }
        }
    )
}

@Composable
fun TileEffectInfo(
    modifier: Modifier = Modifier,
    effectName: String,
    description: String,
    affectedCharacters: List<Character> = emptyList(),
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ){
        Row(
            modifier = Modifier.width(775.dp).height(340.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardBasicInfoColumn(
                modifier = Modifier.width(150.dp).fillMaxHeight(),
                mainText = effectName,
                zoom = 1.0f,
                subText = ""
            )
            Box(
                modifier = Modifier.width(350.dp).fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = description
                )
            }
            AffectedCharactersColumn(
                modifier = Modifier.width(200.dp).fillMaxHeight(),
                characters = affectedCharacters
            )
        }
    }
}