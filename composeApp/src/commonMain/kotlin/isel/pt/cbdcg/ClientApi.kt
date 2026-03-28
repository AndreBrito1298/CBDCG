package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.dto.CreateTableInput
import isel.pt.cbdcg.dto.CreateUserInput
import isel.pt.cbdcg.dto.JoinOrLeaveTableInput
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.TableOutput
import isel.pt.cbdcg.dto.UserOutput
import isel.pt.cbdcg.dto.toTable
import isel.pt.cbdcg.dto.toUser


class ClientApi(private val httpClient: HttpClient) {

    suspend fun createUser(name: String, email: String, password: String): Result<User> = runCatching {

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
            val userOutput = response.body<UserOutput>()
            userOutput.toUser()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun login(email: String, password: String): Result<User> = runCatching {

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
            val userOutput = response.body<UserOutput>()
            userOutput.toUser()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun getTables(): Result<List<Table>> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/tables") {
            contentType(ContentType.Application.Json)
        }

        if (response.status.isSuccess()) {
            val tablesOutputList = response.body<List<TableOutput>>()
            tablesOutputList.map{ it.toTable() }
        } else {
            throw IllegalStateException(response.bodyAsText())
        }
    }

    suspend fun createTable(name: String, owner: User): Result<Table> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/tables/create") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateTableInput(
                    name = name,
                    owner = owner.email.string
                )
            )
        }

        if (response.status.isSuccess()) {
            val tableOutput = response.body<TableOutput>()
            tableOutput.toTable()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }
    }

    suspend fun joinTable(name: String, user: User): Result<Table> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/tables/join") {
            contentType(ContentType.Application.Json)
            setBody(
                JoinOrLeaveTableInput(
                    name = name,
                    user = user.email.string
                )
            )
        }

        if (response.status.isSuccess()) {
            val tableOutput = response.body<TableOutput>()
            tableOutput.toTable()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }
}