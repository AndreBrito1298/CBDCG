package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import isel.pt.cbdcg.dto.CreateUserInput
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.UserOutput


class ClientApi(private val httpClient: HttpClient) {

    suspend fun createUser(name: String, email: String, password: String): Result<UserOutput> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/users") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateUserInput(
                    name = name,
                    email = email,
                    password = password,
                )
            )
        }

        if (response.status.isSuccess()) {
            response.body<UserOutput>()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun login(email: String, password: String): Result<UserOutput> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/users/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginInput(
                    email = email,
                    password = password,
                )
            )
        }

        if (response.status.isSuccess()) {
            response.body<UserOutput>()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }
}