package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.http.ContentType.Application.Json
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.routing.*
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.TableService
import isel.pt.cbdcg.service.UserService
import isel.pt.cbdcg.webapi.tableWebApi
import isel.pt.cbdcg.webapi.userWebApi
import kotlinx.serialization.json.Json

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}


fun Application.module() {
    val httpClient = HttpClient(CIO)

    install(ContentNegotiation) {
        json(
            Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
    }


    installPlugins(httpClient)

    val userService = UserService(UserRepositoryMem)
    val tableService = TableService(UserRepositoryMem, TableRepositoryMem, ParticipantRepositoryMem)

    routing {
        userWebApi(userService, httpClient)
        tableWebApi(tableService)
    }
}
