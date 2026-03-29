package isel.pt.cbdcg.webapi

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headers
import io.ktor.server.auth.OAuthAccessTokenResponse
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import isel.pt.cbdcg.domain.toEmail
import isel.pt.cbdcg.domain.toName
import isel.pt.cbdcg.domain.toPassword
import isel.pt.cbdcg.dto.CreateUserInput
import isel.pt.cbdcg.dto.GoogleUserInfo
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.toAuthUserOutput
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.service.UserService

fun Route.userWebApi(userService: UserService, httpClient: HttpClient) {


    route("/auth") {

        // Google OAuth routes
        authenticate("auth-oauth-google") {
            get("/login/google") {
                // Ktor automatically redirects to Google's authorization URL
            }
            get("/callback") {
                val principal = call.principal<OAuthAccessTokenResponse.OAuth2>()
                    ?: throw UserError.OAuthError("No OAuth principal received")
                val accessToken = principal.accessToken
                val userInfo: GoogleUserInfo = httpClient.get("https://www.googleapis.com/oauth2/v2/userinfo") {
                    headers {
                        append(HttpHeaders.Authorization, "Bearer $accessToken")
                    }
                }.body()
                val authUser = userService.loginOrRegisterWithOAuth(
                    email = userInfo.email.toEmail(),
                    name = userInfo.name.toName(),
                    accessToken
                ).getOrThrow()
                call.respond(HttpStatusCode.OK, authUser.toAuthUserOutput())
            }
        }


        route("/users") {

            post {
                val input = call.receive<CreateUserInput>()

                val result = userService.createUser(
                    name = input.name.toName(),
                    email = input.email.toEmail(),
                    password = input.password.toPassword(),
                ).getOrThrow()

                call.respond(HttpStatusCode.Created, result.toAuthUserOutput())
            }

            post("/login") {
                val input = call.receive<LoginInput>()

                val result = userService.login(
                    email = input.email.toEmail(),
                    password = input.password.toPassword(),
                ).getOrThrow()

                call.respond(HttpStatusCode.Created, result.toAuthUserOutput())
            }
        }
    }
}
