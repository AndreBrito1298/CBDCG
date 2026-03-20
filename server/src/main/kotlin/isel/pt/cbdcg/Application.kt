package isel.pt.cbdcg

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.serialization.kotlinx.json.json
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.TableService
import isel.pt.cbdcg.service.UserService
import isel.pt.cbdcg.webapi.configureRequestHandling
import isel.pt.cbdcg.webapi.tableWebApi
import isel.pt.cbdcg.webapi.userWebApi

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    /**
     * Enables automatic JSON handling.
     */
    install(ContentNegotiation) {
        json()
    }
    configureRequestHandling()

    val userService = UserService(UserRepositoryMem)
    val tableService = TableService(UserRepositoryMem, TableRepositoryMem, ParticipantRepositoryMem)

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        userWebApi(userService)
        tableWebApi(tableService)
    }
}
