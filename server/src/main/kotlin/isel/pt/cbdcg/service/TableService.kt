package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem

class TableService(
    private val userRepo: UserRepositoryMem,
    private val tableRepo: TableRepositoryMem,
    private val participantRepo: ParticipantRepositoryMem
) {

    /**
     * Function to create a Table.
     * @param name The name of the table.
     * @param owner Email of the user that is creating the table.
     */
    fun createTable(name: Name, owner: Email): Result<Participant> = runCatching {

        val owner = userRepo.findByEmail(owner)
        val res = tableRepo.createTable(name, owner.id)

        participantRepo.joinTable(owner, res)
    }

    /**
     * Function to join a table.
     * @param user Email of the user joining the table.
     * @param table The name of the table to join.
     * @throws TableError.UserUnavailable If user is already present in any table.
     */
    fun joinTable(user: Email, table : Name): Result<Participant> = runCatching {

        val user = userRepo.findByEmail(user)
        val table = tableRepo.findByName(table)

        participantRepo.joinTable(user, table)
            .also { syncPlayerCount(table.name) }
    }

    /**
     * Function to leave a table.
     * @param user Email of the user leaving the table.
     * @param name Name of the table to leave.
     */
    fun leaveTable(user: Email, name: Name): Result<Unit> = runCatching {

        val user = userRepo.findByEmail(user)
        val table = tableRepo.findByName(name)

        participantRepo.leaveTable(user, table)

        if(table.owner == user.id)
            deleteTable(table.name, user.email)

        syncPlayerCount(table.name)

    }

    fun changeRole(participant: Email, newRole: Role): Result<Participant> = runCatching {
        val participant = participantRepo.findByEmail(participant)
        participantRepo.changeRole(participant, newRole)
            .also { syncPlayerCount(it.table) }
    }

    fun getAll(): Result<List<Table>> = runCatching {
        tableRepo.getAll()
    }

    fun getParticipants(name: Name): Result<List<Participant>> = runCatching {
        tableRepo.findByName(name)
        participantRepo.findByTable(name)
    }

    fun deleteTable(name: Name, owner: Email): Result<Unit> = runCatching {
        val table = tableRepo.findByName(name)
        val user = userRepo.findByEmail(owner)
        require(table.owner == user.id) { "Only the table owner can delete the table." }
        participantRepo.deleteByTable(name)
        tableRepo.deleteById(table.id)
    }

    private fun syncPlayerCount(name: Name) {
        val players = participantRepo.findByTable(name).count { it.role == Role.PLAYER }.toUInt()
        tableRepo.updatePlayers(name, players)
    }
}
