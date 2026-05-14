package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.game.board.toPosition
import isel.pt.cbdcg.domain.game.decodeCard
import isel.pt.cbdcg.dto.CreateGameDTO
import isel.pt.cbdcg.dto.PlaceOnBoardDTO
import isel.pt.cbdcg.dto.RotatePieceDTO
import isel.pt.cbdcg.error.GameError
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

        post("/place"){

            val input = call.receive<PlaceOnBoardDTO>()

            val (key, value) = input.card.split("|")
            val card = value.decodeCard(key[0])
                ?: throw GameError.CardDoesNotExist(input.card)

            val result = gameService.placeOnBoard(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                card = card,
                idx = input.idx.toUInt(),
                pos = input.pos.toPosition()
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }

        post("/rotate") {

            val input = call.receive<RotatePieceDTO>()

            val result = gameService.rotateTile(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                idx = input.idx.toUInt(),
                right = input.right
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }
    }
}