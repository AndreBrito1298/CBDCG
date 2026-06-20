package isel.pt.cbdcg.views.game.utils.dialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.views.game.utils.misc.info.CharacterInfoPanel
import isel.pt.cbdcg.views.game.utils.misc.info.ItemInfoPanel

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
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                )
            }
        }
    )
}