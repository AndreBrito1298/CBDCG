package isel.pt.cbdcg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import isel.pt.cbdcg.navigation.AppNavHost

@Composable
fun App(client: HttpClient) {

    val clientApi = remember(client) { ClientApi(client) }
    val vm = remember(clientApi) { AppViewModel(clientApi) }

    DisposableEffect(vm) {
        onDispose {
            vm.shutdown()
        }
    }

    MaterialTheme {
        AppNavHost(vm)
    }
}
