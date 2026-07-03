package isel.pt.cbdcg

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import isel.pt.cbdcg.navigation.AppNavHost
import isel.pt.cbdcg.viewmodel.AppViewModel
import isel.pt.cbdcg.viewmodel.AssetRepository
import isel.pt.cbdcg.viewmodel.ClientApi
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage
import isel.pt.cbdcg.views.startMenu.CatalogScreen

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
