package isel.pt.cbdcg.webapi

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.toEmail
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.domain.toPassword
import isel.pt.cbdcg.service.UserService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class CreateUserInput(
    val name: String,
    val email: String,
    val password: String,
)

@Serializable
data class LoginInput(
    val email: String,
    val password: String,
)

fun Route.userWebApi(userService: UserService) {

    route("/users") {

        post {
            val input = call.receive<CreateUserInput>()

            val result = userService.createUser(
                name = input.name.toName(),
                email = input.email.toEmail(),
                password = input.password.toPassword(),
            ).getOrThrow()

            call.respondText(
                text = Json.encodeToString(result.name.string),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.Created,
            )
        }

        post("/login") {
            val input = call.receive<LoginInput>()

            val result = userService.login(
                email = input.email.toEmail(),
                password = input.password.toPassword(),
            ).getOrThrow()

            call.respondText(
                text = Json.encodeToString(result.id),
                contentType = ContentType.Application.Json,
                status = HttpStatusCode.OK,
            )
        }
    }
}
