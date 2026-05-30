package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.game.board.toBoardTile
import isel.pt.cbdcg.domain.game.board.toPosition
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.domain.game.toGameDTO
import isel.pt.cbdcg.dto.CreateGameDTO
import isel.pt.cbdcg.dto.DrawItemDTO
import isel.pt.cbdcg.dto.BoardTileEffectDTO
import isel.pt.cbdcg.dto.NextPhaseDTO
import isel.pt.cbdcg.dto.PlaceOnBoardDTO
import isel.pt.cbdcg.dto.RotatePieceDTO
import isel.pt.cbdcg.service.GameService

fun Route.gameWebApi(gameService: GameService) {

    route("/game") {

        post("/create") {

            val input = call.receive<CreateGameDTO>()

            val result = gameService.createGame(
                tableId = input.tableId.toUInt(),
                userId = input.userId.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toGameDTO())
        }

        post("/place") {

            val input = call.receive<PlaceOnBoardDTO>()

            val card = input.card.toCard()

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

        post("/end-turn") {

            val input = call.receive<NextPhaseDTO>()

            val result = gameService.nextPhase(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }

        post("/move"){

            val input = call.receive<MoveCharacterDTO>()

            /*
            val result = gameService.applyBoardTileEffect(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                effect = CharacterMovement(),
                origin = input.origin.toBoardTile(),
                targets = arrayOf(input.target.toBoardTile())
            ).getOrThrow()
            */
            val result = gameService.moveCharacter(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                from = input.origin.toBoardTile(),
                to = input.target.toBoardTile(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())

        }

        post("/applyBoardEffect") {
            val input = call.receive<BoardTileEffectDTO>()
            val result = gameService.applyBoardTileEffect(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                updaterName = input.updaterName,
                origin = input.origin.toBoardTile(),
                targets = input.target.map { it.toBoardTile() }.toTypedArray(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }

        post("/draw-item"){

            val input = call.receive<DrawItemDTO>()

            val result = gameService.drawItem(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                trigger = input.origin.toBoardTile(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }
    }
}