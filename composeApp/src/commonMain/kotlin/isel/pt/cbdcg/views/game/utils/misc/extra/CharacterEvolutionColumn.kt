package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.character.BattleEvolution
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ItemEvolution
import isel.pt.cbdcg.domain.game.character.MultipleBattlesEvolution
import isel.pt.cbdcg.domain.game.character.description

@Composable
fun CharacterEvolutionColumn(
    modifier: Modifier,
    character: Character,
    getDrawable: suspend (String) -> ImageBitmap,
) {
    val evolution = character.evolution

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (evolution == null) {
            Text(
                text = "Final Evolution",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
            return@Column
        }

        Text(
            text = "Conditions to Evolve",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        when (evolution) {
            is ItemEvolution -> {
                ZoomedImage(
                    fileName = evolution.item,
                    loadDrawable = { getDrawable(evolution.item) },
                    zoom = 0.5f
                )

                Text(
                    text = evolution.item.replace('_', ' '),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            is BattleEvolution -> {
                Text(
                    text = evolution.description(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }

            is MultipleBattlesEvolution -> {
                Text(
                    text = evolution.description(),
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

