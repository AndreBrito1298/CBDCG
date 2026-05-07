package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.ClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readText
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.BoardPosition
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Tile
import isel.pt.cbdcg.dto.CreateGameDTO
import isel.pt.cbdcg.dto.CreateTableDTO
import isel.pt.cbdcg.dto.CreateUserDTO
import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.dto.LoginInput
import isel.pt.cbdcg.dto.LogoutInput
import isel.pt.cbdcg.dto.PlacePieceDTO
import isel.pt.cbdcg.dto.RoleChangeInput
import isel.pt.cbdcg.dto.RotatePieceDTO
import isel.pt.cbdcg.dto.TableOperationInput
import isel.pt.cbdcg.dto.TableDTO
import isel.pt.cbdcg.dto.WsClientMessage
import isel.pt.cbdcg.dto.WsServerMessage
import isel.pt.cbdcg.dto.UserDTO
import isel.pt.cbdcg.dto.toTable
import isel.pt.cbdcg.dto.toUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class ClientApi(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    // Variables with the updated values for the list of tables in the lobby
    private val _tables = MutableStateFlow<List<Table>>(emptyList())
    val tables = _tables.asStateFlow()

    // Variable with the updated info of a specific table
    private val _currentTable = MutableStateFlow<Table?>(null)
    val currentTable = _currentTable.asStateFlow()

    private val _game = MutableStateFlow<Game?>(null)

    val game = _game.asStateFlow()

    // Session opened with the Server
    private var socketSession: ClientWebSocketSession? = null

    // Coroutine that is currently listening to any Server update message
    private var listenJob: Job? = null

    // Translates a message and updates the local state ('tables' or 'currentTable')
    private fun handleServerMessage(text: String) {

        val message = json.decodeFromString<WsServerMessage>(text)

        when (message) {

            is WsServerMessage.LobbyTables -> {
                _tables.value = message.tables.map { it.toTable() }
            }

            is WsServerMessage.TableInfo -> {
                _currentTable.value = message.table.toTable()
            }

            is WsServerMessage.TableDeleted -> {
                val current = _currentTable.value
                if (current?.id?.toInt() == message.tableId) {
                    _currentTable.value = null
                }
            }

            is WsServerMessage.GameInfo -> {
                _game.value = message.game.toGame()
            }
        }
    }

    // Guarantees that there is an open session and an active listener to that same session
    suspend fun ensureConnected() {

        if (socketSession != null) return

        val session = client.webSocketSession("ws://localhost:$SERVER_PORT/ws")
        socketSession = session

        listenJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                for (frame in session.incoming) {
                    if (frame !is Frame.Text) continue
                    handleServerMessage(frame.readText())
                }
            } finally {
                socketSession = null
                listenJob = null
            }
        }
    }

    // Functions to start listening to updates in either the 'Lobby' or a specific 'Table'

    suspend fun subscribeLobby() {

        ensureConnected()

        val payload = json.encodeToString<WsClientMessage>(
            WsClientMessage.SubscribeLobby
        )

        socketSession?.send(Frame.Text(payload))
    }
    suspend fun subscribeTable(tableName: String) {

        ensureConnected()

        val payload = json.encodeToString<WsClientMessage>(
            WsClientMessage.SubscribeTable(tableName)
        )

        socketSession?.send(Frame.Text(payload))
    }
    suspend fun subscribeGame(gameId: UInt) {

        ensureConnected()

        val payload = json.encodeToString<WsClientMessage>(
            WsClientMessage.SubscribeGame(gameId.toInt())
        )

        socketSession?.send(Frame.Text(payload))
    }
    suspend fun disconnectAll() {
        listenJob?.cancel()
        listenJob = null

        socketSession?.close()
        socketSession = null
    }

    // HTTP connection with the Server

    suspend fun createUser(name: Name, email: Email, password: Password): Result<User> =
        fetch<UserDTO>(
            path = "auth/users/create",
            method = HttpMethod.Post,
            body = CreateUserDTO(name.string, email.string, password.string)
        ).map { it.toUser() }
    suspend fun login(email: Email, password: Password): Result<User> =
        fetch<UserDTO>(
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
        fetch<Array<TableDTO>>(
            path = "tables",
            method = HttpMethod.Get
        ).map { it.map{ tableOutput -> tableOutput.toTable() } }


    suspend fun createTable(tableName: Name, id: UInt, token: String): Result<Table> =
        fetch<TableDTO>(
            path = "tables/create",
            method = HttpMethod.Post,
            body = CreateTableDTO(tableName.string, id.toInt(), token)
        ).map{ it.toTable() }
    suspend fun joinTable(userId: UInt, tableId: UInt, token: String): Result<Table> =
        fetch<TableDTO>(
            path = "tables/join",
            method = HttpMethod.Post,
            body = TableOperationInput(tableId.toInt(), userId.toInt(), token)
        ).map{ it.toTable() }
    suspend fun leaveTable(userId: UInt, tableId: UInt, token: String): Result<Unit> =
        fetch<Unit>(
            path = "tables/leave",
            method = HttpMethod.Post,
            body = TableOperationInput(tableId.toInt(), userId.toInt(), token)
        )
    suspend fun changeRole(userId: UInt, tableId: UInt, token: String, role: Role): Result<Unit> =
        fetch<Unit>(
            path = "tables/change-role",
            method = HttpMethod.Post,
            body = RoleChangeInput(tableId.toInt(), userId.toInt(), token, role.toString())
        )

    suspend fun createGame(tableId: UInt, userId: UInt, token: String): Result<Game> =
        fetch<GameDTO>(
            path = "game/create",
            method = HttpMethod.Post,
            body = CreateGameDTO(userId.toInt(), token, tableId.toInt())
        ).map{ it.toGame() }
    suspend fun placeTile(userId: UInt, gameId: UInt, token: String, tile: Tile, idx: UInt, pos: BoardPosition): Result<Game> =
        fetch<GameDTO>(
            path = "game/place",
            method = HttpMethod.Post,
            body = PlacePieceDTO(userId.toInt(), gameId.toInt(), token, tile.codeString(), idx.toInt(), pos.coords())
        ).map{ it.toGame() }
    suspend fun rotateTile(userId: UInt, gameId: UInt, token: String, idx: UInt, right: Boolean): Result<Game> =
        fetch<GameDTO>(
            path = "game/rotate",
            method = HttpMethod.Post,
            body = RotatePieceDTO(userId.toInt(), gameId.toInt(), token, idx.toInt(), right)
        ).map{ it.toGame() }

    private suspend inline fun <reified T> fetch(
        path: String,
        method: HttpMethod,
        body: Any? = null,
        // query: Map<String, String> = emptyMap(),
    ): Result<T> = runCatching {

        val response = client.request("http://localhost:$SERVER_PORT/$path") {
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