package isel.pt.cbdcg

import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import isel.pt.cbdcg.error.Error
import isel.pt.cbdcg.error.ParticipantError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError

fun Application.installPlugins() {

    install(ContentNegotiation) {
        json()
    }

    install(StatusPages) {

        exception<IllegalArgumentException>{ call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid format.")
        }

        exception<BadRequestException>{ call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid request body.")
        }

        exception<Error>{ call, cause ->
            val (status, message) = cause.toHttpResponse()
            call.respond(status, message)
        }

        exception<Throwable> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError, "Internal server error.")
        }
    }
}

fun Error.toHttpResponse(): Pair<HttpStatusCode, String> =
    when (this) {
        is UserError.DuplicateEmail -> HttpStatusCode.Conflict to (message ?: desc)
        is UserError.EmailNotFound -> HttpStatusCode.NotFound to (message ?: desc)
        is UserError.PasswordMismatch -> HttpStatusCode.Unauthorized to (message ?: desc)
        is TableError.DuplicateName -> HttpStatusCode.Conflict to (message ?: desc)
        is TableError.UserUnavailable -> HttpStatusCode.Conflict to (message ?: desc)
        is TableError.UserNotFound -> HttpStatusCode.NotFound to (message ?: desc)
        is TableError.TableDoesNotExist -> HttpStatusCode.NotFound to (message ?: desc)
        is ParticipantError.ParticipantEmailNotFound -> HttpStatusCode.NotFound to (message ?: desc)
        is ParticipantError.ParticipantIdNotFound -> HttpStatusCode.NotFound to (message ?: desc)
        is ParticipantError.UserNotOnTable -> HttpStatusCode.NotFound to (message ?: desc)
    }
