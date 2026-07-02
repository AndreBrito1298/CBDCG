package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.ParticipantRepository
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.repository.database.ParticipantRepositoryDB
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.webapi.websocket.EventsPublisher
import kotlin.collections.plus
import kotlin.runCatching

class TableService(
    private val userRepo: UserRepository,
    private val tableRepo: TableRepository,
    private val participantRepo: ParticipantRepository,
    private val events: EventsPublisher,
) {

    suspend fun getTables(): Result<List<Table>> = runCatching {
        tableRepo.getAllTables()
    }
    suspend fun createTable(tableName: Name, userId: UInt): Result<Table> = runCatching {
        val owner = userRepo.findById(userId)
            ?: throw UserError.NotLoggedIn()

        val participant = Participant(owner, Role.PLAYER)

        val table = tableRepo.createTable(tableName, owner, participant)
        participantRepo.createParticipant(owner ,table ,Role.PLAYER)

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(table)

        table
    }
    suspend fun joinTable(userId: UInt, tableId: UInt): Result<Table> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        if(user.auth == null) throw UserError.NotLoggedIn()

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(!participantRepo.userAvailability(user))
            throw TableError.UserUnavailable(user.name.toString())

        val role =
            if(table.participants.size < 4) Role.PLAYER
            else Role.SPECTATOR
        val participant = participantRepo.createParticipant(user, table, role)

        val newTable = table.copy(participants = table.participants.plus(participant))
        tableRepo.save(newTable)

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(newTable)

        newTable
    }
    suspend fun leaveTable(userId: UInt, tableId: UInt): Result<Unit> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        if(user.auth == null) throw UserError.NotLoggedIn()

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(isParticipant(user, table) == null)
            throw TableError.UserNotFound(user.name.toString(), table.name.toString())

        if(table.owner == user) {
            table.participants.forEach { participantRepo.deleteParticipant(it.user) }
            tableRepo.deleteById(table.id)

            events.publishTableDeleted(table)
        } else {
            val newTable = tableRepo.removeParticipant(table, user)
            participantRepo.deleteParticipant(user)

            events.publishTableUpdated(newTable)
        }

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
    }
    suspend fun changeRole(userId: UInt, tableId: UInt, role: Role): Result<Unit> = runCatching  {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        if(user.auth == null) throw UserError.NotLoggedIn()

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        val participant = isParticipant(user, table)
            ?: throw TableError.UserNotFound(user.name.toString(), table.name.toString())

        val newTable = tableRepo.updateParticipants(table, participant.copy(role = role))

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(newTable)
    }

    private fun isParticipant(user: User, table: Table): Participant? {
        return table.participants.find{ it.user.id == user.id }
    }
}
