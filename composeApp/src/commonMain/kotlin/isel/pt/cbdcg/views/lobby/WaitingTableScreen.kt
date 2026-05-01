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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

@Composable
fun WaitingTableScreen(
    user: User,
    table: Table,
    changeRole: (Role) -> Unit,
    leaveTable: () -> Unit,
    createGame: () -> Unit
){

    val participants = table.participants

    val players = participants
        .filter { it.role == Role.PLAYER || it.role == Role.READY }
        .map { it.user to if(it.role == Role.READY) true else false }
    val spectators = participants
        .filter { it.role == Role.SPECTATOR }
        .map { it.user to null }

    val participant = participants.first{ it.user.id == user.id }
    val startFlag = participant.user.id == table.owner.id && players.all{ it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = table.name.string,
                style = MaterialTheme.typography.headlineMedium,
            )

            Button(
                onClick = leaveTable,
            ) {
                Text("Leave Table")
            }
        }

        Text(
            text = "Owner: ${table.owner.name}[#${table.owner.id}]",
            style = MaterialTheme.typography.headlineSmall,
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = { changeRole(Role.PLAYER) },
                enabled = participant.role != Role.PLAYER && participant.role != Role.READY,
                modifier = Modifier.weight(1f),
            ) {
                Text("Join Players")
            }

            Button(
                onClick = { changeRole(Role.SPECTATOR) },
                enabled = participant.role != Role.SPECTATOR,
                modifier = Modifier.weight(1f),
            ) {
                Text("Join Spectators")
            }
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

        Button(
            onClick = {

                val role =
                    if(participant.role == Role.READY) Role.PLAYER
                    else Role.READY

                if (startFlag) createGame()
                else changeRole(role)
            },
            enabled = participant.role != Role.SPECTATOR,
            modifier = Modifier.fillMaxWidth(),
        ) {

            val text =
                if(participant.role == Role.READY && !startFlag) "Ready"
                else if(startFlag) "Start Game"
                else "Not Ready"

            Text(text)
        }
    }
}

@Composable
private fun WaitingColumn(title: String, users: List<Pair<User, Boolean?>>, modifier: Modifier = Modifier) {
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
                users.forEach { (user, ready) ->

                    val readyColor =
                        if(ready == null) Color.Black
                        else if (ready) Color.Green else Color.Red

                    Text(
                        text = "${user.name.string} [#${user.id}]",
                        color = readyColor,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}