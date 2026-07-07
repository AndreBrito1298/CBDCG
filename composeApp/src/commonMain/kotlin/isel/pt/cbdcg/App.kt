package isel.pt.cbdcg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import isel.pt.cbdcg.navigation.AppNavHost
import isel.pt.cbdcg.viewmodel.AppViewModel
import isel.pt.cbdcg.viewmodel.AssetRepository
import isel.pt.cbdcg.viewmodel.ClientApi

@Composable
fun App(client: HttpClient) {

    val clientApi = remember(client) { ClientApi(client) }
    val assetRepository = remember(client) {
        AssetRepository(
            client = client,
            serverUrl = "http://localhost:$SERVER_PORT"
        )
    }
    val vm = remember(clientApi, assetRepository) {
        AppViewModel(clientApi, assetRepository)
    }

    MaterialTheme {
        AppNavHost(vm)
    }
}
