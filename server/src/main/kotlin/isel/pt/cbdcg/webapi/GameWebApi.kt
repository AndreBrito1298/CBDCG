package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.dto.CreateGameDTO
import isel.pt.cbdcg.dto.toGameDTO
import isel.pt.cbdcg.service.GameService

fun Route.gameWebApi(gameService: GameService) {

    route("/game") {

        post("/create"){

            val input = call.receive<CreateGameDTO>()

            val result = gameService.createGame(
                tableId = input.tableId.toUInt(),
                userId = input.userId.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toGameDTO())

        }
    }
}