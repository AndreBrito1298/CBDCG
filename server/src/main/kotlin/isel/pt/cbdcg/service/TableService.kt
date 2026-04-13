package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.events.TableEventsPublisher
import kotlin.collections.plus
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

    suspend fun createTableWithEmail(tableName: Name, userEmail: Email, token: String): Result<Table> = runCatching {
        val owner = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)
        token.verifyToken(owner)

        return finishTableCreation(tableName, owner)
    }

    suspend fun createTableWithID(tableName: Name, userID: UInt, token: String): Result<Table> = runCatching {
        val owner = userRepo.findById(userID)
            ?: throw UserError.IdNotFound()
        token.verifyToken(owner)

        return finishTableCreation(tableName, owner)
    }

    suspend fun finishTableCreation(tableName: Name, owner: User): Result<Table> = runCatching {
        val participant = participantRepo.createParticipant(owner, Role.PLAYER)
        val table = tableRepo.createTable(tableName, owner, participant)
        val tables = tableRepo.getAllTables()
        events.publishLobbyTables(tables)
        events.publishTableUpdated(table)
        table
    }


    suspend fun joinTableWithEmailAndName(userEmail: Email, tableName: Name, token: String): Result<Table> = runCatching {
        val user = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)
        token.verifyToken(user)
        val table = tableRepo.findByName(tableName)
            ?: throw TableError.TableDoesNotExist(tableName.string)
        return finishJoiningTable(user, table)
    }

    suspend fun joinTableWithID(userID: UInt, tableID: UInt, token: String): Result<Table> = runCatching {
        val user = userRepo.findById(userID)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)
        val table = tableRepo.findById(tableID)
            ?: throw TableError.TableDoesNotExist(tableID.toString())

        return finishJoiningTable(user, table)
    }

    suspend fun finishJoiningTable(user: User, table: Table): Result<Table> = runCatching {
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

    suspend fun leaveTableWithEmailAndName(userEmail: Email, tableName: Name, token: String): Result<Unit> = runCatching {
        val user = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)
        token.verifyToken(user)
        val table = tableRepo.findByName(tableName)
            ?: throw TableError.TableDoesNotExist(tableName.string)
        finishLeavingTable(user, table)
    }
    suspend fun leaveTableWithID(userID: UInt, tableID: UInt, token: String): Result<Unit> = runCatching {
        val user = userRepo.findById(userID)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)
        val table = tableRepo.findById(tableID)
            ?: throw TableError.TableDoesNotExist(tableID.toString())
        finishLeavingTable(user, table)
    }

    suspend fun finishLeavingTable(user: User, table: Table): Result<Unit> = runCatching {
        if(isParticipant(user, table) != null)
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


    suspend fun changeRoleWithEmailAndName(userEmail: Email, tableName: Name, token: String): Result<Unit> = runCatching {
        val user = userRepo.findByEmail(userEmail)
            ?: throw UserError.EmailNotFound(userEmail.string)
        token.verifyToken(user)
        val table = tableRepo.findByName(tableName)
            ?: throw TableError.TableDoesNotExist(tableName.string)
        val participant = table.participants.find{ it.user == user }
            ?: throw TableError.UserNotFound(user.name.toString(), table.name.toString())
        finishRoleChange(table, participant)
    }

    suspend fun changeRoleWithID(userID: UInt, tableID: UInt, token: String): Result<Unit> = runCatching {
        val user = userRepo.findById(userID)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)
        val table = tableRepo.findById(tableID)
            ?: throw TableError.TableDoesNotExist(tableID.toString())
        val participant = isParticipant(user, table)?:
        throw TableError.UserNotFound(user.name.toString(), table.name.toString())
        finishRoleChange(table, participant)
    }

    private suspend fun finishRoleChange(table: Table, participant: Participant) {
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

    private fun isParticipant(user: User, table: Table): Participant? {
        return table.participants.find{ it.user == user }
    }



}
