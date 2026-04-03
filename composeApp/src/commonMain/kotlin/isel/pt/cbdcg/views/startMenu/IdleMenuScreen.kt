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
import isel.pt.cbdcg.domain.User

@Composable
fun IdleMenuScreen(
    user: User?,
    loginNav: () -> Unit,
    createUserNav: () -> Unit,
    searchTablesNav: () -> Unit,
    logout: () -> Unit
){

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

        if(user == null){

            Button(
                onClick = loginNav,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log In")
            }

            Button(
                onClick = createUserNav,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Create User")
            }

        } else {

            Button(
                onClick = searchTablesNav,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Search Tables to Play")
            }

            Button(
                onClick = logout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Log Out")
            }

        }
    }
}