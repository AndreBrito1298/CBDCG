package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.BadRequestException
import isel.pt.cbdcg.repository.Error
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

@Serializable
data class HttpErrorResponse(
    val message: String,
)

fun Throwable.toHttpError(): Pair<HttpStatusCode, HttpErrorResponse> =
    when (this) {
        is IllegalArgumentException ->
            HttpStatusCode.BadRequest to HttpErrorResponse("Invalid format.")
        is BadRequestException ->
            HttpStatusCode.BadRequest to HttpErrorResponse("Invalid request body.")
        is Error ->
            HttpStatusCode.BadRequest to HttpErrorResponse(message ?: desc)
        else ->
            HttpStatusCode.InternalServerError to HttpErrorResponse("Unexpected Error.")
    }
