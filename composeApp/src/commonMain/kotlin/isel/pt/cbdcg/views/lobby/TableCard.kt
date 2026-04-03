package isel.pt.cbdcg.views.lobby

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.Table

@Composable
fun TableCard(
    table: Table,
    joinTable: () -> Unit,
) {

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
                    text = "Table: '${table.name.string} | Owner: '${table.owner.name.string} [#${table.owner.id}]'",
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = "Number of Players: ${table.participants.size}",
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
            ) {
                Button(
                    onClick = joinTable
                ) {
                    Text("Join")
                }
            }
        }
    }
}