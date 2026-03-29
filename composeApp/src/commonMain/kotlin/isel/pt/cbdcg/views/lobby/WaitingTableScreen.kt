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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.ClientApi
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.views.utils.displayError
import kotlinx.coroutines.launch

@Composable
fun WaitingTableScreen(clientApi: ClientApi, user: AuthUser, participant: Participant, exit: () -> Unit){

    val scope = rememberCoroutineScope()

    var participants by remember { mutableStateOf<List<Participant>>(emptyList()) }
    val players = participants
        .filter { it.role == Role.PLAYER }
        .map { it.user.string }
    val spectators = participants
        .filter { it.role == Role.SPECTATOR }
        .map { it.user.string }

    val currentParticipant = participants.find { it.user == user.email }
    val currentRole = currentParticipant?.role ?: participant.role

    var isLoading by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }

    var error by remember { mutableStateOf<String?>(null) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        isLoading = true

        val result = clientApi.getParticipants(participant.table.string)

        result.onSuccess {
            participants = it
            error = null
        }
        result.onFailure {
            error = it.message ?: "Could not load participants."
        }

        isLoading = false
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = participant.table.string,
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
                onClick = {
                    scope.launch {
                        isSubmitting = true

                        val result = clientApi.changeRole(user.email.string, Role.PLAYER.name)

                        result.onSuccess {
                            error = null
                            refreshKey += 1
                        }
                        result.onFailure {
                            error = it.message ?: "Could not join players."
                        }

                        isSubmitting = false
                    }
                },
                enabled = currentRole != Role.PLAYER,
                modifier = Modifier.weight(1f),
            ) {
                Text("Join Players")
            }

            Button(
                onClick = {
                    scope.launch {
                        isSubmitting = true

                        val result = clientApi.changeRole(user.email.string, Role.SPECTATOR.name)

                        result.onSuccess {
                            error = null
                            refreshKey += 1
                        }
                        result.onFailure {
                            error = it.message ?: "Could not join players."
                        }

                        isSubmitting = false
                    }
                },
                enabled = currentRole != Role.SPECTATOR,
                modifier = Modifier.weight(1f),
            ) {
                Text("Join Spectators")
            }
        }

        Button(
            onClick = {
                scope.launch {

                    isSubmitting = true

                    val result = clientApi.leaveTable(user.email.string, participant.table.string)

                    result.onSuccess {
                        exit()
                    }
                    result.onFailure {
                        error = it.message ?: "Could not leave table."
                    }

                    isSubmitting = false
                }
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Leave Table")
        }

        if (isLoading) {
            Text("Loading participants...")
        } else {
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

        displayError(error)

    }
}

@Composable
private fun WaitingColumn(title: String, users: List<String>, modifier: Modifier = Modifier) {
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
                users.forEach { name ->
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}