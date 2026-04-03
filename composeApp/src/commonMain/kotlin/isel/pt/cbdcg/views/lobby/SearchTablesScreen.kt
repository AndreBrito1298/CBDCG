package isel.pt.cbdcg.views.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

@Composable
fun SearchTablesScreen(
    user: User,
    tables: List<Table>,
    mainMenuNav: () -> Unit,
    joinTable: (Table) -> Unit,
    createTable: (String) -> Unit,
) {

    var tables by remember { mutableStateOf<List<Table>>(tables) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Button(onClick = mainMenuNav) {
            Text("Back")
        }

        Text(
            text = "Available Tables",
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "User: ${user.name.string}",
            style = MaterialTheme.typography.headlineSmall,
        )


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
