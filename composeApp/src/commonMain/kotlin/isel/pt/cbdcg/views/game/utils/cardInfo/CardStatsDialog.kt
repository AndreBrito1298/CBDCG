package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard

@Composable
fun CardStatsPanel(
    card: Card,
    unequip: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (card) {
        is CharacterCard -> CharacterInfoPanel(
            character = card.character,
            unequip = { idx -> unequip(idx) },
            modifier = modifier
        )

        is ItemCard -> ItemInfoPanel(
            item = card.item,
            modifier = modifier
        )

        else -> Unit
    }
}

@Composable
fun CardStatsDialog(
    card: Card,
    unequip: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    println("DEBUG: Opening CardStatsDialog for card: ${card}")
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Box(
                modifier = Modifier
                    .width(720.dp)
                    .height(340.dp)
            ) {
                CardStatsPanel(
                    card = card,
                    unequip = { idx -> unequip(idx) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}