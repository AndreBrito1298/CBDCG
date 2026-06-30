package isel.pt.cbdcg.views.game.utils.players

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.character.PlayableCharacter

@Composable
fun PlayerHand(
    getDrawable: suspend (String) -> ImageBitmap,
    hand: Map<UInt, Card>,
    selectCard: (UInt, Card) -> Unit,
    selected: UInt?,
    placeCard: () -> Unit,
    inspectCard: (Card) -> Unit,
    rotateLeft: () -> Unit,
    rotateRight: () -> Unit,
){
    if (hand.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Sem cartas na mão")
        }
    } else {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            hand.forEach { (index, card) ->
                when (card) {
                    is TileCard -> PlayerTileCard(
                        getDrawable = { getDrawable(it) },
                        tile = card.tile,
                        select = { selectCard(index, card) },
                        isSelected = if (selected == null) false else selected == index,
                        place = placeCard,
                        rotateLeft = rotateLeft,
                        rotateRight = rotateRight,
                        inspect = { inspectCard(card) },
                    )

                    is CharacterCard -> PlayerCharacterCard(
                        getDrawable = { getDrawable(it) },
                        character = card.character as PlayableCharacter,
                        select = { selectCard(index, card) },
                        isSelected = if (selected == null) false else selected == index,
                        place = placeCard,
                        inspect = { inspectCard(card) },
                    )

                    is ItemCard -> PlayerItemCard(
                        getDrawable = { getDrawable(it) },
                        item = card.item,
                        select = { selectCard(index, card) },
                        isSelected = if (selected == null) false else selected == index,
                        equip = placeCard,
                        inspect = { inspectCard(card) },
                    )
                }
            }
        }
    }
}