package isel.pt.cbdcg.views.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.views.lobby.utils.CreateTableCard
import isel.pt.cbdcg.views.lobby.utils.TableCard

@Composable
fun SearchTablesScreen(
    user: User,
    tables: List<Table>,
    joinTable: (Table) -> Unit,
    createTable: (String) -> Unit,
    logout: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {

        Text(
            text = "Available Tables",
            style = MaterialTheme.typography.headlineMedium,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            Text(
                text = "User: ${user.name.string}",
                style = MaterialTheme.typography.headlineSmall,
            )

            Button(onClick = logout) {
                Text("Logout")
            }
        }

        tables.forEach { table ->
            TableCard(
                table = table,
                joinTable = { joinTable(table) }
            )
        }

        CreateTableCard(
            createTable = { name -> createTable(name) }
        )
    }
}
