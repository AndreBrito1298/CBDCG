package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.verifyToken
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.webapi.websocket.EventsPublisher
import kotlin.collections.plus
import kotlin.runCatching

class TableService(
    private val userRepo: UserRepositoryMem,
    private val tableRepo: TableRepositoryMem,
    private val participantRepo: ParticipantRepositoryMem,
    private val events: EventsPublisher,
) {

    fun getTables(): Result<List<Table>> = runCatching {
        tableRepo.getAllTables()
    }
    suspend fun createTable(tableName: Name, userId: UInt, token: String): Result<Table> = runCatching {

        val owner = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(owner)

        val participant = participantRepo.createParticipant(owner, Role.PLAYER)
        val table = tableRepo.createTable(tableName, owner, participant)

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(table)

        table
    }
    suspend fun joinTable(userId: UInt, tableId: UInt, token: String): Result<Table> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(!participantRepo.userAvailability(user))
            throw TableError.UserUnavailable(user.name.toString())

        val role =
            if(table.participants.size < 4) Role.PLAYER
            else Role.SPECTATOR
        val participant = participantRepo.createParticipant(user, role)

        val newTable = table.copy(participants = table.participants.plus(participant))
        tableRepo.save(newTable)

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(newTable)

        newTable
    }
    suspend fun leaveTable(userID: UInt, tableID: UInt, token: String): Result<Unit> = runCatching {

        val user = userRepo.findById(userID)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val table = tableRepo.findById(tableID)
            ?: throw TableError.TableDoesNotExist(tableID.toString())

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
    suspend fun changeRole(userID: UInt, tableID: UInt, token: String): Result<Unit> = runCatching  {

        val user = userRepo.findById(userID)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val table = tableRepo.findById(tableID)
            ?: throw TableError.TableDoesNotExist(tableID.toString())

        val participant = isParticipant(user, table) ?:
            throw TableError.UserNotFound(user.name.toString(), table.name.toString())

        val newRole =
            if(participant.role == Role.PLAYER) Role.SPECTATOR
            else Role.PLAYER

        val newTable = tableRepo.updateParticipants(table, participant.copy(role = newRole))

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(newTable)
    }

    private fun isParticipant(user: User, table: Table): Participant? {
        return table.participants.find{ it.user.id == user.id }
    }
}
