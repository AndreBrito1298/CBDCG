package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import isel.pt.cbdcg.domain.game.board.TileEffect

@Composable
fun TileEffectDialog(
    effect: TileEffect,
    confirm: () -> Unit,
    dismiss: () -> Unit,
){
    AlertDialog(
        onDismissRequest = dismiss,
        confirmButton = {
            TextButton(onClick = confirm) {
                Text("Activate")
            }
        },
        dismissButton = {
            TextButton(onClick = dismiss) {
                Text("Ignore")
            }
        },
        text = {
            Box(modifier = Modifier.width(640.dp).height(320.dp)){
                TileEffectInfo(
                    modifier = Modifier.fillMaxSize(),
                    effectName = effect.type.name,
                    description = effect.info
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
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center
    ){
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CardBasicInfoColumn(
                modifier = Modifier.width(180.dp).height(320.dp),
                mainText = effectName,
                zoom = 1.0f,
                subText = ""
            )
            EffectDescription(
                description = description,
                modifier = Modifier.width(360.dp).height(320.dp)
            )
        }
    }
}