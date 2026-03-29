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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.ClientApi
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.views.utils.displayError

@Composable
fun SearchTablesScreen(clientApi: ClientApi, user: AuthUser, back: () -> Unit, join: (Participant) -> Unit) {

    var tables by remember { mutableStateOf<List<Table>>(emptyList()) }

    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }
    var refresh by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {

        val result = clientApi.getTables()
        result.onSuccess { tables = it ; error = null }
        result.onFailure { error = it.message ?: "Could not load tables." }

        isLoading = false
        refresh = false

    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {

        Button(onClick = back) {
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

        when {
            isLoading -> Text("Loading tables...")

            else -> {
                tables.forEach { table ->
                    TableCard(clientApi, table, user) { participant -> join(participant) }
                }
            }
        }

        CreateTableCard(
            clientApi,
            user,
            { error = it },
            { table -> join(table) }
        )

        displayError(error)
    }
}
