package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.toEmail
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.domain.toRole
import isel.pt.cbdcg.dto.ChangeRoleInput
import isel.pt.cbdcg.dto.CreateTableInput
import isel.pt.cbdcg.dto.JoinOrLeaveTableInput
import isel.pt.cbdcg.dto.toParticipantOutput
import isel.pt.cbdcg.dto.toTableOutput
import isel.pt.cbdcg.service.TableService

fun Route.tableWebApi(tableService: TableService) {

    route("/tables") {

        get {

            val result = tableService.getAll().getOrThrow()
            call.respond(HttpStatusCode.OK, result.map{ it.toTableOutput() })

        }

        post("/create") {

            val input = call.receive<CreateTableInput>()

            val result = tableService.createTable(
                name = input.name.toName(),
                owner = input.owner.toEmail(),
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toParticipantOutput())
        }

        post("/join") {

            val input = call.receive<JoinOrLeaveTableInput>()

            val result = tableService.joinTable(
                user = input.user.toEmail(),
                table = input.name.toName(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toParticipantOutput())
        }

        post("/leave") {

            val input = call.receive<JoinOrLeaveTableInput>()

            tableService.leaveTable(
                user = input.user.toEmail(),
                name = input.name.toName(),
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }

        get("/participants") {

            val input = call.request.queryParameters["name"]
                ?: error("Request query parameter 'name' missing")

            val result = tableService.getParticipants(
                name = input.toName(),
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.map { it.toParticipantOutput() })
        }

        post("/changeRole") {

            val input = call.receive<ChangeRoleInput>()

            val result = tableService.changeRole(
                participant = input.name.toEmail(),
                newRole = input.role.toRole()?: Role.SPECTATOR,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toParticipantOutput())
        }
    }
}
