package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.events.TableEventsPublisher
import kotlin.runCatching

class TableService(
    private val userRepo: UserRepositoryMem,
    private val tableRepo: TableRepositoryMem,
    private val participantRepo: ParticipantRepositoryMem,
    private val events: TableEventsPublisher,
) {

    fun getTables(): Result<List<Table>> = runCatching {
        tableRepo.getAllTables()
    }

    suspend fun createTable(tableName: Name, userEmail: Email, token: String): Result<Table> = runCatching {

        val owner = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)

        token.verifyToken(owner)

        val participant = participantRepo.createParticipant(owner, Role.PLAYER)
        val table = tableRepo.createTable(tableName, owner, participant)

        val tables = tableRepo.getAllTables()

        events.publishLobbyTables(tables)
        events.publishTableUpdated(table)

        table
    }

    suspend fun joinTable(userEmail: Email, tableName: Name, token: String): Result<Table> = runCatching {

        val user = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)

        token.verifyToken(user)

        val table = tableRepo.findByName(tableName)
            ?: throw TableError.TableDoesNotExist(tableName.string)

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

    suspend fun leaveTable(userEmail: Email, tableName: Name, token: String): Result<Unit> = runCatching {

        val user = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)

        token.verifyToken(user)

        val table = tableRepo.findByName(tableName)
            ?: throw TableError.TableDoesNotExist(tableName.string)

        if(table.participants.find{ it.user == user } == null)
            throw TableError.UserNotFound(user.name.toString(), table.name.toString())

        val newTable = tableRepo.removeParticipant(table, user)
        participantRepo.deleteParticipant(user)

        if(table.owner == user) {
            table.participants.forEach { participantRepo.deleteParticipant(it.user) }
            tableRepo.deleteById(table.id)
        } else {
            events.publishTableUpdated(newTable)
        }

        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)

    }

    suspend fun changeRole(userEmail: Email, tableName: Name, token: String): Result<Unit> = runCatching {

        val user = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)

        token.verifyToken(user)

        val table = tableRepo.findByName(tableName)
            ?: throw TableError.TableDoesNotExist(tableName.string)

        val participant = table.participants.find{ it.user == user }
            ?: throw TableError.UserNotFound(user.name.toString(), table.name.toString())

        val newRole =
            if(participant.role == Role.PLAYER) Role.SPECTATOR
            else Role.PLAYER

        val newTable = tableRepo.updateParticipants(table, participant.copy(role = newRole))

        val tables = tableRepo.getAllTables()

        events.publishLobbyTables(tables)
        events.publishTableUpdated(newTable)
    }

    /*
    private fun syncPlayerCount(name: Name) {
        val players = participantRepo.findByTable(name).count { it.role == Role.PLAYER }.toUInt()
        tableRepo.updatePlayers(name, players)
    }
    */

    private fun String.verifyToken(user: User) {

        if(user.auth == null)
            throw UserError.TokenNotFound()

        if(user.auth!!.token != this)
            throw UserError.TokenMismatch()

    }

}
