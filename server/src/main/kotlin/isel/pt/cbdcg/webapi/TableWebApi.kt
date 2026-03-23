package isel.pt.cbdcg.webapi

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.toEmail
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.domain.toRole
import isel.pt.cbdcg.service.TableService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CreateTableInput(
    val name: String,
    val owner: String,
)

@Serializable
data class JoinOrLeaveTableInput(
    val name: String,
    val owner: String,
)

@Serializable
data class ChangeRoleInput(
    val name: String,
    val role: String,
)


fun Route.tableWebApi(tableService: TableService) {
    route("/tables") {
        post {
            val input = call.receive<CreateTableInput>()

            val result = tableService.createTable(
                name = input.name.toName(),
                owner = input.owner.toEmail(),
            ).getOrThrow()

            call.respondText(
                text = Json.encodeToString(result.name.string),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.Created,
            )
        }

        post("/join") {
            val input = call.receive<JoinOrLeaveTableInput>()

            tableService.joinTable(
                user = input.owner.toEmail(),
                table = input.name.toName(),
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }

        post("/leave") {
            val input = call.receive<JoinOrLeaveTableInput>()

            tableService.leaveTable(
                user = input.owner.toEmail(),
                name = input.name.toName(),
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }

        post("/changeRole") {
            val input = call.receive<ChangeRoleInput>()

            tableService.changeRole(
                participant = input.name.toEmail(),
                newRole = input.role.toRole()?: Role.SPECTATOR,
            )
        }
    }
}
