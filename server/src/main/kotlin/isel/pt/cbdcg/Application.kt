package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.CacheControl
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.repository.database.GameRepositoryDB
import isel.pt.cbdcg.repository.database.ParticipantRepositoryDB
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import isel.pt.cbdcg.repository.memory.GameRepositoryMem
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.GameService
import isel.pt.cbdcg.service.TableService
import isel.pt.cbdcg.service.UserService
import isel.pt.cbdcg.webapi.gameWebApi
import isel.pt.cbdcg.webapi.tableWebApi
import isel.pt.cbdcg.webapi.userWebApi
import isel.pt.cbdcg.webapi.websocket.WebSocketHub
import isel.pt.cbdcg.webapi.websocket.webSocketApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

fun main() {
    // dbInit(true)
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}
fun Application.module() {

    val httpClient = HttpClient(CIO)

    installPlugins(httpClient)

    val webSocketHub = WebSocketHub()

    val userService = UserService(UserRepositoryMem)
    val tableService = TableService(UserRepositoryMem, TableRepositoryMem, ParticipantRepositoryMem, webSocketHub)
    val gameService =
        GameService(
            GameRepositoryMem,
            ParticipantRepositoryMem,
            TableRepositoryMem,
            UserRepositoryMem,
            webSocketHub,
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        )

    val cleanupScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    cleanupScope.launch {
        while (true) {
            delay(TIME_BETWEEN_CLEANUP.milliseconds)
            userService.deleteInactiveUsers()
        }
    }

    monitor.subscribe(ApplicationStopped) {
        cleanupScope.cancel()
    }

    routing {
        userWebApi(userService, httpClient)
        tableWebApi(tableService)
        gameWebApi(gameService)
        webSocketApi(webSocketHub)

        staticResources("/assets", "game-assets") {
            cacheControl {
                listOf(CacheControl.MaxAge(maxAgeSeconds = 60 * 60 * 24 * 30))
            }
        }
    }
}
