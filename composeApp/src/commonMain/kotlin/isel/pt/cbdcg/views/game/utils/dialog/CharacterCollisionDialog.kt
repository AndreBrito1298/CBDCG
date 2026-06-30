package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.misc.extra.SimpleClock
import isel.pt.cbdcg.views.game.utils.misc.info.CardBasicInfoColumn
import isel.pt.cbdcg.views.game.utils.misc.stats.CardStatsColumn

enum class CollisionOption {
    COMBAT, SNEAK, CANCEL
}

@Composable
fun CharacterCollisionDialog(
    getDrawable: suspend (String) -> ImageBitmap,
    remainingSeconds: Long,
    movingCharacter: Character,
    staticCharacter: Character,
    canSneak: Boolean,
    onClick: (CollisionOption) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically){
                Box(modifier = Modifier.weight(8f)){
                    Text(
                        text = "Character Collision",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(modifier = Modifier.weight(2f)){
                    SimpleClock(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(50.dp)
                            .padding(4.dp),
                        remainingSeconds = remainingSeconds
                    )
                }
            }
        },
        text = {
            Box(modifier = Modifier.width(800.dp).height(340.dp)){
                CharacterCollisionInfo(
                    getDrawable,
                    Modifier.fillMaxSize(),
                    movingCharacter,
                    staticCharacter,
                    canSneak,
                    onClick
                )
            }
        },
        confirmButton = {  }
    )
}

@Composable
fun CharacterCollisionInfo(
    getDrawable: suspend (String) -> ImageBitmap,
    modifier: Modifier = Modifier,
    movingCharacter: Character,
    staticCharacter: Character,
    canSneak: Boolean,
    onClick: (CollisionOption) -> Unit,
){
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
        ){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {

                CardBasicInfoColumn(
                    modifier = Modifier.width(160.dp).fillMaxHeight(),
                    getDrawable = { getDrawable(it) },
                    mainText = movingCharacter.name,
                    zoom = 1f,
                    subText = "Attacker"
                )

                CardStatsColumn(
                    modifier = Modifier.width(190.dp).fillMaxHeight(),
                    stats = movingCharacter.adjustStats()
                )

                Box(
                    modifier = Modifier.width(60.dp).fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "VS",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.Red
                    )
                }

                CardStatsColumn(
                    modifier = Modifier.width(190.dp).fillMaxHeight(),
                    stats = staticCharacter.adjustStats()
                )

                CardBasicInfoColumn(
                    getDrawable = { getDrawable(it) },
                    modifier = Modifier.width(160.dp).fillMaxHeight(),
                    mainText = staticCharacter.name,
                    zoom = 1f,
                    subText = "Defender"
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                Button(onClick = { onClick(CollisionOption.COMBAT) }) { Text("Combat") }
                Button(onClick = { onClick(CollisionOption.SNEAK) }, enabled = canSneak) { Text("Sneak") }
                Button(onClick = { onClick(CollisionOption.CANCEL) }) { Text("Cancel") }
            }
        }
    }
}