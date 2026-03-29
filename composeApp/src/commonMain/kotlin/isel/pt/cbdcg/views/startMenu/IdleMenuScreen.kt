package isel.pt.cbdcg.views.startMenu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.views.PossibleStates

@Composable
fun IdleMenuScreen(go: (PossibleStates) -> Unit){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ){

        Text(
            text = "Welcome",
            style = MaterialTheme.typography.headlineMedium,
        )

        Button(
            onClick = { go(PossibleStates.Login) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log In")
        }

        Button(
            onClick = { go(PossibleStates.Create) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create User")
        }

    }

}