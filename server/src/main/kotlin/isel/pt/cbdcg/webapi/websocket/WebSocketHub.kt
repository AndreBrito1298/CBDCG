package isel.pt.cbdcg.webapi.websocket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.toGameDTO
import isel.pt.cbdcg.domain.toTableDTO
import isel.pt.cbdcg.dto.WsServerMessage
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

class WebSocketHub(
    val json: Json = Json { ignoreUnknownKeys = true }
): EventsPublisher {

    private val mutex = Mutex()

    private val lobbySessions = mutableSetOf<DefaultWebSocketServerSession>()
    private val tableSessions = mutableMapOf<String, MutableSet<DefaultWebSocketServerSession>>()

    private val gameSessions = mutableMapOf<Int, MutableSet<DefaultWebSocketServerSession>>()
    suspend fun registerLobby(session: DefaultWebSocketServerSession) {
        mutex.withLock {
            // Stop listening to updates in any 'table'
            tableSessions.values.forEach{ it.remove(session) }
            // Stop listening to updates in any 'game'
            gameSessions.values.forEach{ it.remove(session) }
            lobbySessions.add(session)
        }
    }
    suspend fun registerTable(tableName: String, session: DefaultWebSocketServerSession) {
        mutex.withLock {
            // Stop listening to updates in the 'lobby'
            lobbySessions.remove(session)
            val sessions = tableSessions.getOrPut(tableName) { mutableSetOf() }
            sessions.add(session)
        }
    }
    suspend fun registerGame(gameId: Int, session: DefaultWebSocketServerSession) {
        mutex.withLock {
            tableSessions.values.forEach{ it.remove(session) }
            val sessions = gameSessions.getOrPut(gameId) { mutableSetOf() }
            sessions.add(session)
        }
    }
    suspend fun unregisterAll(session: DefaultWebSocketServerSession) {
        mutex.withLock {
            lobbySessions.remove(session)
            tableSessions.values.forEach { it.remove(session) }
            gameSessions.values.forEach { it.remove(session) }
        }
    }


    override suspend fun publishLobbyTables(tables: List<Table>) {

        val message = WsServerMessage.LobbyTables(tables.map { it.toTableDTO() })
        val payload = json.encodeToString<WsServerMessage>(message)

        val sessions = mutex.withLock { lobbySessions.toList() }
        sessions.forEach { session ->
            session.send(Frame.Text(payload))
        }
    }
    override suspend fun publishTableUpdated(table: Table) {

        val message = WsServerMessage.TableInfo(table.toTableDTO())
        val payload = json.encodeToString<WsServerMessage>(message)

        val sessions = mutex.withLock {
            tableSessions[table.name.string]?.toList().orEmpty()
        }
        sessions.forEach { session ->
            session.send(Frame.Text(payload))
        }
    }
    override suspend fun publishGameStarted(table: Table, game: Game) {

        val message = WsServerMessage.GameInfo(game.toGameDTO())
        val payload = json.encodeToString<WsServerMessage>(message)

        val sessions = mutex.withLock {
            tableSessions[table.name.string]?.toList().orEmpty()
        }
        sessions.forEach{ session ->
            session.send(Frame.Text(payload))
        }

        mutex.withLock {
            tableSessions.remove(table.name.string)
        }
    }
    override suspend fun publishGameUpdated(game: Game) {

        val message = WsServerMessage.GameInfo(game.toGameDTO())
        val payload = json.encodeToString<WsServerMessage>(message)

        val sessions = mutex.withLock {
            gameSessions[game.id.toInt()]?.toList().orEmpty()
        }
        sessions.forEach{ session ->
            session.send(Frame.Text(payload))
        }

    }
    override suspend fun publishTableDeleted(table: Table) {

        val message = WsServerMessage.TableDeleted(
            tableId = table.id.toInt()
        )
        val payload = json.encodeToString<WsServerMessage>(message)

        val sessions = mutex.withLock {
            tableSessions[table.name.string]?.toList().orEmpty()
        }
        sessions.forEach { session ->
            session.send(Frame.Text(payload))
        }

        mutex.withLock {
            tableSessions.remove(table.name.string)
        }
    }

}