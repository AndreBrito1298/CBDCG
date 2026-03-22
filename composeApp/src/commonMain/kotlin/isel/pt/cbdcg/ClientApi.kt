package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable


class ClientApi(
    private val httpClient: HttpClient,
) {
    suspend fun createUser(
        name: String,
        email: String,
        password: String,
    ): ApiResult<String> =

        try {

            val response = httpClient.post("http://localhost:$SERVER_PORT/users") {
                contentType(ContentType.Application.Json)
                setBody(
                    CreateUserRequest(
                        name = name,
                        email = email,
                        password = password,
                    ),
                )
            }

            ApiResult.Success(response.body())

        } catch (e: ClientRequestException) {

            val message = try {
                e.response.body<HttpErrorResponse>().message
            } catch (_: Throwable) {
                "Request failed with status ${e.response.status.value}."
            }

            ApiResult.Failure(message)

        } catch (_: Throwable) {
            ApiResult.Failure("Could not reach the server.")
        }
}

// Body format defined in the DTO.
@Serializable
data class CreateUserRequest(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class HttpErrorResponse(
    val message: String,
)

sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val message: String) : ApiResult<Nothing>
}
