package isel.pt.cbdcg.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.ClientApi
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import kotlinx.coroutines.launch

@Composable
fun TableCard(
    clientApi: ClientApi,
    table: Table,
    user: User,
) {

    val scope = rememberCoroutineScope()

    Card(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = table.name.string,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "Players: ${table.players}",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Button(
                    onClick = { scope.launch{ clientApi.joinTable(table.name.string, user) } },
                ) {
                    Text("Join")
                }
            }
        }
    }
}