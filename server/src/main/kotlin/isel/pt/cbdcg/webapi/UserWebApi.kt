package isel.pt.cbdcg.webapi

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.toEmail
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.domain.toPassword
import isel.pt.cbdcg.dto.CreateUserInput
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.toUserOutput
import isel.pt.cbdcg.service.UserService

fun Route.userWebApi(userService: UserService) {

    route("/users") {

        post {
            val input = call.receive<CreateUserInput>()

            val result = userService.createUser(
                name = input.name.toName(),
                email = input.email.toEmail(),
                password = input.password.toPassword(),
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toUserOutput())
        }

        post("/login") {
            val input = call.receive<LoginInput>()

            val result = userService.login(
                email = input.email.toEmail(),
                password = input.password.toPassword(),
            ).getOrThrow()

            call.respond(HttpStatusCode.Created, result.toUserOutput())
        }
    }
}
