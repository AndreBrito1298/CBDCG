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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    loginNav: () -> Unit,
    createUserNav: () -> Unit,
    catalogNav: () -> Unit,
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ){

        Text(
            text = "Card-Based Dungeon Crawling Game",
            style = MaterialTheme.typography.headlineMedium,
        )

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

        Button(
            onClick = catalogNav,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Game Catalog")
        }
    }
}