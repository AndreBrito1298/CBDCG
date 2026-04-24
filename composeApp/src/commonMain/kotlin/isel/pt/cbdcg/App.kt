package isel.pt.cbdcg

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import io.ktor.client.HttpClient
import isel.pt.cbdcg.domain.game.Board
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.navigation.AppNavHost
import isel.pt.cbdcg.views.game.GameScreen

@Composable
fun App(client: HttpClient) {

    val clientApi = remember(client) { ClientApi(client) }
    val vm = remember(clientApi) { AppViewModel(clientApi) }

    MaterialTheme {
        // AppNavHost(vm)


        GameScreen(
            game = Game(0u, listOf(), Board(), 0u),
            {  }
        )
    }
}
