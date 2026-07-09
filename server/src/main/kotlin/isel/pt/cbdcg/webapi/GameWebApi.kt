package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.game.board.toPosition
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.domain.game.toGameDTO
import isel.pt.cbdcg.dto.CreateGameDTO
import isel.pt.cbdcg.dto.GameRecoveryDTO
import isel.pt.cbdcg.dto.GameUpdaterDTO
import isel.pt.cbdcg.dto.SimpleGameRequestDTO
import isel.pt.cbdcg.dto.PlaceOnBoardDTO
import isel.pt.cbdcg.dto.RotatePieceDTO
import isel.pt.cbdcg.dto.UnequipItemDTO
import isel.pt.cbdcg.service.GameService

fun Route.gameWebApi(gameService: GameService) {

    route("/game") {

        get{
            val input = call.receive<GameRecoveryDTO>()

            val result = gameService.getGame(input.userId.toUInt())
                .getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }

        post("/create") {

            val input = call.receive<CreateGameDTO>()

            val result = gameService.createGame(
                tableId = input.tableId.toUInt(),
                userId = input.userId.toUInt(),
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

            val input = call.receive<SimpleGameRequestDTO>()

            val result = gameService.nextPhase(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }

        post("/leave"){

            val input = call.receive<SimpleGameRequestDTO>()

            gameService.leaveGame(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)

        }

        post("/unequip"){

            val input = call.receive<UnequipItemDTO>()

            val result = gameService.unequip(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                character = input.character.toCharacter(),
                index = input.index,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())

        }

        post("/applyGameUpdater") {
            val input = call.receive<GameUpdaterDTO>()
            val result = gameService.applyGameUpdater(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                updaterName = input.updaterName,
                origin = input.origin.toType(),
                targets = input.target.map { it.toType() },
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }
    }
}

/*
   post("/update-modifiers"){

            val input = call.receive<SimpleInGameActionDTO>()

            val result = gameService.updateStatModifiers(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                origin = input.origin.toBoardTile(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())

        }

        post("/draw-item"){

            val input = call.receive<SimpleInGameActionDTO>()

            val result = gameService.drawItem(
                userId = input.userId.toUInt(),
                gameId = input.gameId.toUInt(),
                token = input.token,
                trigger = input.origin.toBoardTile(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toGameDTO())
        }


  route("/battle"){
            post{
                val input = call.receive<StartBattleDTO>()

                val result = gameService.battle(
                    userId = input.userId.toUInt(),
                    gameId = input.gameId.toUInt(),
                    token = input.token,
                    attacker = input.attacker.toCharacter(),
                    defender = input.defender.toCharacter(),
                ).getOrThrow()

                call.respond(HttpStatusCode.OK, result.toGameDTO())
            }

            post("/participate"){

                val input = call.receive<ParticipateInBattleDTO>()

                val result = gameService.participateInBattle(
                    userId = input.userId.toUInt(),
                    gameId = input.gameId.toUInt(),
                    token = input.token,
                    character = input.character.toCharacter(),
                    accept = input.accept,
                ).getOrThrow()

                call.respond(HttpStatusCode.OK, result.toGameDTO())
            }

            post("/act"){

                val input = call.receive<ActInBattleDTO>()

                val result = gameService.actInBattle(
                    userId = input.userId.toUInt(),
                    gameId = input.gameId.toUInt(),
                    token = input.token,
                    action = input.action.toPossibleBattleAction(),
                    origin = input.origin.toCharacter(),
                    target = input.target?.toCharacter(),
                ).getOrThrow()

                call.respond(HttpStatusCode.OK, result.toGameDTO())
            }

            post("/undo"){

                val input = call.receive<ActInBattleDTO>()

                val result = gameService.undoBattleAction(
                    userId = input.userId.toUInt(),
                    gameId = input.gameId.toUInt(),
                    token = input.token,
                    origin = input.origin.toCharacter(),
                ).getOrThrow()

                call.respond(HttpStatusCode.OK, result.toGameDTO())
            }

            post("/leave"){

                val input = call.receive<ActInBattleDTO>()

                val result = gameService.leaveBattle(
                    userId = input.userId.toUInt(),
                    gameId = input.gameId.toUInt(),
                    token = input.token,
                    playerCharacter = input.origin.toCharacter(),
                    action = input.action.toPossibleBattleAction(),
                ).getOrThrow()

                call.respond(HttpStatusCode.OK, result.toGameDTO())
            }
        }
 */