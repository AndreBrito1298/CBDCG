package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.dto.CreateTableInput
import isel.pt.cbdcg.dto.CreateUserInput
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.ChangeRoleInput
import isel.pt.cbdcg.dto.LogoutInput
import isel.pt.cbdcg.dto.TableOperationInput
import isel.pt.cbdcg.dto.TableOutput
import isel.pt.cbdcg.dto.UserOutput
import isel.pt.cbdcg.dto.toTable
import isel.pt.cbdcg.dto.toUser


class ClientApi(private val httpClient: HttpClient) {

    suspend fun createUser(name: Name, email: Email, password: Password): Result<User> =
        fetch<UserOutput>(
            path = "auth/users/create",
            method = HttpMethod.Post,
            body = CreateUserInput(name.string, email.string,password.string)
        ).map { it.toUser() }

    suspend fun login(email: Email, password: Password): Result<User> =
        fetch<UserOutput>(
            path = "auth/users/login",
            method = HttpMethod.Post,
            body = LoginInput(email.string, password.string)
        ).map { it.toUser() }

    suspend fun logout(token: String): Result<Unit> =
        fetch<Unit>(
            path = "auth/users/logout",
            method = HttpMethod.Post,
            body = LogoutInput(token)
        )

    suspend fun getTables(): Result<List<Table>> =
        fetch<Array<TableOutput>>(
            path = "tables",
            method = HttpMethod.Get
        ).map { it.map{ tableOutput -> tableOutput.toTable() } }

    suspend fun createTable(tableName: Name, userEmail: Email, token: String): Result<Table> =
        fetch<TableOutput>(
            path = "tables/create",
            method = HttpMethod.Post,
            body = CreateTableInput(tableName.string, userEmail.string, token)
        ).map{ it.toTable() }

    suspend fun joinTable(tableName: Name, userEmail: Email, token: String): Result<Table> =
        fetch<TableOutput>(
            path = "tables/join",
            method = HttpMethod.Post,
            body = TableOperationInput(tableName.string, userEmail.string, token)
        ).map{ it.toTable() }

    suspend fun leaveTable(userEmail: Email, tableName: Name, token: String): Result<Unit> =
        fetch<Unit>(
            path = "tables/leave",
            method = HttpMethod.Post,
            body = TableOperationInput(tableName.string, userEmail.string, token)
        )

    suspend fun changeRole(userEmail: Email, tableName: Name, token: String): Result<Unit> =
        fetch<Unit>(
            path = "tables/change-role",
            method = HttpMethod.Post,
            body = ChangeRoleInput(userEmail.string, tableName.string, token)
        )

    private suspend inline fun <reified T> fetch(
        path: String,
        method: HttpMethod,
        body: Any? = null,
        // query: Map<String, String> = emptyMap(),
    ): Result<T> = runCatching {

        val response = httpClient.request("http://localhost:$SERVER_PORT/$path") {
            this.method = method
            contentType(ContentType.Application.Json)

            /*
            if(query.isNotEmpty()) {
                url{
                    query.forEach{ (key, value) -> parameters.append(key, value) }
                }
            }
            */

            if(body != null) setBody(body)

        }

        if (!response.status.isSuccess()) {
            throw IllegalStateException(response.bodyAsText())
        }

        if (T::class == Unit::class) Unit as T
        else response.body<T>()
    }

}
