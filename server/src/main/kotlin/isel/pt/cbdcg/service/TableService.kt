package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.error.ParticipantError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.runCatching

class TableService(
    private val userRepo: UserRepositoryMem,
    private val tableRepo: TableRepositoryMem,
    private val participantRepo: ParticipantRepositoryMem
) {


    fun getAll(): Result<List<Table>> = runCatching {
        tableRepo.getAllTables()
    }

    /**
     * Function to create a Table.
     * @param name The name of the table.
     * @param owner Email of the user that is creating the table.
     */
    fun createTable(name: Name, owner: Email): Result<Participant> = runCatching {

        val owner = userRepo.findByEmail(owner)
            ?: throw UserError.EmailNotFound(owner.string)

        val res = tableRepo.createTable(name, owner.id)
        participantRepo.joinTable(owner, res, Role.PLAYER)
    }

    /**
     * Function to join a table.
     * @param user Email of the user joining the table.
     * @param table The name of the table to join.
     * @throws TableError.UserUnavailable If user is already present in any table.
     * @throws TableError.TableDoesNotExist If the table doesn't exist.
     * @throws UserError.EmailNotFound No user with the provided email exists.
     */
    fun joinTable(user: Email, table : Name): Result<Participant> = runCatching {
        val user = userRepo.findByEmail(user)
            ?: throw UserError.EmailNotFound(user.string)

        val table = tableRepo.findByName(table)
            ?: throw TableError.TableDoesNotExist(table.string)

        val availability = participantRepo.userAvailability(user)
        if(availability != null)
            throw TableError.UserUnavailable(user.name.toString(), availability.toString())
        val role =
            if(tableRepo.addPlayerToTable(table) <= 4.toUInt()) Role.PLAYER
            else Role.SPECTATOR
        participantRepo.joinTable(user, table, role)
    }

    /**
     * Function to leave a table.
     * @param user Email of the user leaving the table.
     * @param name Name of the table to leave.
     * @throws TableError.UserNotFound If user is not in the specified table.
     * @throws TableError.TableDoesNotExist If the table doesn't exist.
     * @throws UserError.EmailNotFound No user with the provided email exists.
     */
    fun leaveTable(user: Email, name: Name): Result<Unit> = runCatching {
        val user = userRepo.findByEmail(user)
            ?: throw UserError.EmailNotFound(user.string)

        val table = tableRepo.findByName(name)
            ?: throw TableError.TableDoesNotExist(name.string)

        if(!participantRepo.findUserInTable(user, table))
            throw TableError.UserNotFound(user.name.toString(), table.name.toString())

        participantRepo.leaveTable(user, table)

        if(table.owner == user.id)
            deleteTable(table.name, user.email)

        syncPlayerCount(table.name)

    }

    /**
     * Function to change a participant's role.
     * @param participant Email of the participant.
     * @param newRole The new role to assign.
     * @throws ParticipantError.ParticipantEmailNotFound If participant is not found.
     */
    fun changeRole(participant: Email, newRole: Role) = runCatching {
        val participant = participantRepo.findByEmail(participant)
            ?: throw ParticipantError.ParticipantEmailNotFound(participant.toString())

        participantRepo.changeRole(participant, newRole)
            .also { syncPlayerCount(it.table) }
    }


    fun getParticipants(name: Name): Result<List<Participant>> = runCatching {
        tableRepo.findByName(name)
        participantRepo.findByTable(name)
    }

    fun deleteTable(name: Name, owner: Email): Result<Unit> = runCatching {
        val table = tableRepo.findByName(name)
        val user = userRepo.findByEmail(owner)
        require(table!!.owner == user!!.id) { "Only the table owner can delete the table." }
        participantRepo.deleteByTable(name)
        tableRepo.deleteById(table.id)
    }

    private fun syncPlayerCount(name: Name) {
        val players = participantRepo.findByTable(name).count { it.role == Role.PLAYER }.toUInt()
        tableRepo.updatePlayers(name, players)
    }
}
