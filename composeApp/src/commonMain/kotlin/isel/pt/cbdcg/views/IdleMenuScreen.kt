package isel.pt.cbdcg.views

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

@Composable
fun IdleMenuScreen(go: (MainMenuStates) -> Unit){


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
            onClick = { go(MainMenuStates.Login) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Log In")
        }

        Button(
            onClick = { go(MainMenuStates.Create) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Create User")
        }

        Button(
            onClick = { go(MainMenuStates.SearchTables) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Search Tables")
        }

    }

}