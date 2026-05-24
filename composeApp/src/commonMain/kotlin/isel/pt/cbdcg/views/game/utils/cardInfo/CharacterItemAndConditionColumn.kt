package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun CharacterItemAndConditionColumn(
    modifier: Modifier,
    character: Character,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .border(1.dp, Color.LightGray)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        if(character is PlayableCharacter){
            val equippedItem = character.items.firstOrNull()
            if (equippedItem != null) {
                Text(
                    text = "Equipped",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )

                ZoomedImage(
                    fileName = equippedItem.name,
                    zoom = 1.25f,
                    select = { },
                    canSelect = false
                )

                Text(
                    text = equippedItem.name.replace('_', ' '),
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp
                )
            }
        }

        // Condição de Evoluir
    }
}