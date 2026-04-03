package isel.pt.cbdcg.views.lobby

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

@Composable
fun WaitingTableScreen(
    user: User,
    table: Table,
    changeRole: () -> Unit,
    leaveTable: () -> Unit
){

    var participants by remember { mutableStateOf<List<Participant>>(table.participants) }

    val players = participants
        .filter { it.role == Role.PLAYER }
        .map { it.user }
    val spectators = participants
        .filter { it.role == Role.SPECTATOR }
        .map { it.user }

    val currentRole = if(players.find{ it.id == user.id } != null) Role.PLAYER else Role.SPECTATOR

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = table.name.string,
            style = MaterialTheme.typography.headlineMedium,
        )

        Text(
            text = "User: ${user.name.string}",
            style = MaterialTheme.typography.headlineSmall,
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = changeRole,
                enabled = currentRole != Role.PLAYER,
                modifier = Modifier.weight(1f),
            ) {
                Text("Join Players")
            }

            Button(
                onClick = changeRole,
                enabled = currentRole != Role.SPECTATOR,
                modifier = Modifier.weight(1f),
            ) {
                Text("Join Spectators")
            }
        }

        Button(
            onClick = leaveTable,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Leave Table")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            WaitingColumn(
                title = "Players",
                users = players,
                modifier = Modifier.weight(1f),
            )

            WaitingColumn(
                title = "Spectators",
                users = spectators,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WaitingColumn(title: String, users: List<User>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.widthIn(min = 180.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )

            if (users.isEmpty()) {
                Text(
                    text = "No users yet.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                users.forEach { user ->
                    Text(
                        text = "${user.name.string} [#${user.id}]", // temporary?
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}