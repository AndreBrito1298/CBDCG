package isel.pt.cbdcg.webapi

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureRequestHandling() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val (status, response) = cause.toHttpError()
            call.respond(status, response)
        }
    }
}
