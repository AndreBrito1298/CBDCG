package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import isel.pt.cbdcg.configs.dbInit
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
import org.jetbrains.exposed.v1.jdbc.Database

fun main() {
    dbInit()
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {

    val httpClient = HttpClient(CIO)

    installPlugins(httpClient)

    val webSocketHub = WebSocketHub()

    val userService = UserService(UserRepositoryMem)
    val tableService = TableService(UserRepositoryMem, TableRepositoryMem, ParticipantRepositoryMem, webSocketHub)
    val gameService = GameService(GameRepositoryMem, TableRepositoryMem, UserRepositoryMem, webSocketHub)

    routing {
        userWebApi(userService, httpClient)
        tableWebApi(tableService)
        gameWebApi(gameService)
        webSocketApi(webSocketHub)
    }
}
