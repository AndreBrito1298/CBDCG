package isel.pt.cbdcg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient

@Composable
fun App(client: HttpClient) {

    val clientApi = remember(client) { ClientApi(client) }

    MaterialTheme {
        CreateUserScreen(clientApi)
    }
}
