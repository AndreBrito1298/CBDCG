package isel.pt.cbdcg.webapi.websocket

import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.websocket.Frame
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.dto.TableWsServerMessage
import isel.pt.cbdcg.dto.toTableOutput
import isel.pt.cbdcg.service.events.TableEventsPublisher
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.json.Json

/*
ESTA VERSÃO NÃO ESTÁ IMUNE A "FALHAS DE ENVIO", POR EXEMPLO, O CLIENTE PERDER A CONEXÃO AO SERVER
DEVE SER FEITO UM MECANISMO QUE, AO VERIFICAR QUE UM DOS CLIENTES REGISTADOS NÃO RECEBEU A NOTIFICAÇÃO
POR ALGUM MOTIVO, RETIRA A SESSÃO DO CLIENTE OU ALGO DO GÉNERO.
*/

class WebSocketHub(
    val json: Json = Json { ignoreUnknownKeys = true }
): TableEventsPublisher {

    private val mutex = Mutex()

    private val lobbySessions = mutableSetOf<DefaultWebSocketServerSession>()
    private val tableSessions = mutableMapOf<String, MutableSet<DefaultWebSocketServerSession>>()

    suspend fun registerLobby(session: DefaultWebSocketServerSession) {
        mutex.withLock {
            // Stop listening to updates in any 'table'
            tableSessions.values.forEach{ it.remove(session) }
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

    suspend fun unregisterAll(session: DefaultWebSocketServerSession) {
        mutex.withLock {
            lobbySessions.remove(session)
            tableSessions.values.forEach { it.remove(session) }
        }
    }

    override suspend fun publishLobbyTables(tables: List<Table>) {

        val message = TableWsServerMessage.LobbyTables(
            tables = tables.map { it.toTableOutput() }
        )
        val payload = json.encodeToString<TableWsServerMessage>(message)

        val sessions = mutex.withLock { lobbySessions.toList() }
        sessions.forEach { session ->
            session.send(Frame.Text(payload))
        }
    }

    override suspend fun publishTableUpdated(table: Table) {

        val message = TableWsServerMessage.TableInfo(
            table = table.toTableOutput()
        )
        val payload = json.encodeToString<TableWsServerMessage>(message)

        val sessions = mutex.withLock {
            tableSessions[table.name.string]?.toList().orEmpty()
        }
        sessions.forEach { session ->
            session.send(Frame.Text(payload))
        }
    }

}