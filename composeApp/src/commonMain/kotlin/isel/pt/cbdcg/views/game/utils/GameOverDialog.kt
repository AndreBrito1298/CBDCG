package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Player

@Composable
fun GameOverDialog(
    winner: Player,
    onDismiss: () -> Unit,
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Game Over",
                style = MaterialTheme.typography.headlineMedium
            )
        },
        text = {
            Column {
                Text(
                    text = "Vencedor: ${winner.user.name.string}",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(text = "O jogo terminou! Podes voltar ao lobby.")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Sair do Jogo")
            }
        }
    )
}