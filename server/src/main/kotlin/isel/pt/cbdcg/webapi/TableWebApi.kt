package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.toEmail
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.dto.CreateTableInput
import isel.pt.cbdcg.dto.TableOperationInput
import isel.pt.cbdcg.dto.toTableOutput
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.service.TableService

fun Route.tableWebApi(tableService: TableService) {

    route("/tables") {

        get {

            val result = tableService.getAll().getOrThrow()
            call.respond(HttpStatusCode.OK, result.map{ it.toTableOutput() })

        }

        post("/create") {

            val input = call.receive<CreateTableInput>()
            val token = call.request.queryParameters["token"]
                ?: throw UserError.TokenNotFound()

            val result = tableService.createTable(
                name = input.name.toName(),
                email = input.owner.toEmail(),
                token = token,
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toTableOutput())
        }

        post("/join") {

            val input = call.receive<TableOperationInput>()
            val token = call.request.queryParameters["token"]
                ?: throw UserError.TokenNotFound()

            val result = tableService.joinTable(
                userEmail = input.user.toEmail(),
                tableName = input.table.toName(),
                token = token,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toTableOutput())
        }

        post("/leave") {

            val input = call.receive<TableOperationInput>()
            val token = call.request.queryParameters["token"]
                ?: throw UserError.TokenNotFound()

            tableService.leaveTable(
                userEmail = input.user.toEmail(),
                tableName = input.table.toName(),
                token = token,
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }

        post("/changeRole") {

            val input = call.receive<TableOperationInput>()
            val token = call.request.queryParameters["token"]
                ?: throw UserError.TokenNotFound()

            val result = tableService.changeRole(
                userEmail = input.user.toEmail(),
                tableName = input.table.toName(),
                token = token,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toTableOutput())
        }
    }
}
