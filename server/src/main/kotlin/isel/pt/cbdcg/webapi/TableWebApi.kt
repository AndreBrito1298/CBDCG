package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.dto.CreateTableDTO
import isel.pt.cbdcg.dto.TableOperationInput
import isel.pt.cbdcg.dto.toTableDTO
import isel.pt.cbdcg.service.TableService

fun Route.tableWebApi(tableService: TableService) {

    route("/tables") {

        get {

            val result = tableService.getTables().getOrThrow()
            call.respond(HttpStatusCode.OK, result.map{ it.toTableDTO() })

        }

        post("/create") {

            val input = call.receive<CreateTableDTO>()

            val result = tableService.createTable(
                tableName = input.name.toName(),
                userId = input.userId.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toTableDTO())
        }

        post("/join") {

            val input = call.receive<TableOperationInput>()

            val result = tableService.joinTable(
                userId = input.user.toUInt(),
                tableId = input.table.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.respond(HttpStatusCode.OK, result.toTableDTO())
        }

        post("/leave") {

            val input = call.receive<TableOperationInput>()

            tableService.leaveTable(
                userId = input.user.toUInt(),
                tableId = input.table.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }

        post("/change-role") {

            val input = call.receive<TableOperationInput>()

            tableService.changeRole(
                userId = input.user.toUInt(),
                tableId = input.table.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }

        post("/ready") {

            val input = call.receive<TableOperationInput>()

            tableService.toggleReady(
                userId = input.user.toUInt(),
                tableId = input.table.toUInt(),
                token = input.token,
            ).getOrThrow()

            call.response.status(HttpStatusCode.OK)
        }
    }
}
