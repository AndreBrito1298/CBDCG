package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.dto.AuthUserOutput
import isel.pt.cbdcg.dto.CreateTableInput
import isel.pt.cbdcg.dto.CreateUserInput
import isel.pt.cbdcg.dto.JoinOrLeaveTableInput
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.ChangeRoleInput
import isel.pt.cbdcg.dto.ParticipantOutput
import isel.pt.cbdcg.dto.TableOutput
import isel.pt.cbdcg.dto.toAuthUser
import isel.pt.cbdcg.dto.toParticipant
import isel.pt.cbdcg.dto.toTable


class ClientApi(private val httpClient: HttpClient) {

    suspend fun createUser(name: String, email: String, password: String): Result<AuthUser> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/auth/users") {
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
            val userOutput = response.body<AuthUserOutput>()
            userOutput.toAuthUser()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun login(email: String, password: String): Result<AuthUser> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/auth/users/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginInput(
                    email = email,
                    password = password
                )
            )
        }

        if (response.status.isSuccess()) {
            val userOutput = response.body<AuthUserOutput>()
            userOutput.toAuthUser()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun getTables(): Result<List<Table>> = runCatching {

        val response = httpClient.get("http://localhost:$SERVER_PORT/tables") {
            contentType(ContentType.Application.Json)
        }

        if (response.status.isSuccess()) {
            val tablesOutputList = response.body<List<TableOutput>>()
            tablesOutputList.map{ it.toTable() }
        } else {
            throw IllegalStateException(response.bodyAsText())
        }
    }

    suspend fun createTable(name: String, owner: String): Result<Participant> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/tables/create") {
            contentType(ContentType.Application.Json)
            setBody(
                CreateTableInput(
                    name = name,
                    owner = owner
                )
            )
        }

        if (response.status.isSuccess()) {
            val tableOutput = response.body<ParticipantOutput>()
            tableOutput.toParticipant()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }
    }

    suspend fun joinTable(name: String, user: String): Result<Participant> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/tables/join") {
            contentType(ContentType.Application.Json)
            setBody(
                JoinOrLeaveTableInput(
                    name = name,
                    user = user
                )
            )
        }

        if (response.status.isSuccess()) {
            val participantOutput = response.body<ParticipantOutput>()
            participantOutput.toParticipant()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun getParticipants(table: String): Result<List<Participant>> = runCatching {

        val response = httpClient.get("http://localhost:$SERVER_PORT/tables/participants") {
            url {
                parameters.append("name", table)
            }
        }

        if (response.status.isSuccess()) {
            val participantsOutput = response.body<List<ParticipantOutput>>()
            participantsOutput.map { it.toParticipant() }
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun changeRole(user: String, role: String): Result<Participant> = runCatching {

        val response = httpClient.post("http://localhost:$SERVER_PORT/tables/changeRole") {
            contentType(ContentType.Application.Json)
            setBody(
                ChangeRoleInput(
                    name = user,
                    role = role
                )
            )
        }

        if (response.status.isSuccess()) {
            val participantOutput = response.body<ParticipantOutput>()
            participantOutput.toParticipant()
        } else {
            throw IllegalStateException(response.bodyAsText())
        }

    }

    suspend fun leaveTable(user: String, table: String) = runCatching {

        httpClient.post("http://localhost:$SERVER_PORT/tables/leave") {
            contentType(ContentType.Application.Json)
            setBody(
                JoinOrLeaveTableInput(
                    name = table,
                    user = user
                )
            )
        }

    }

}
