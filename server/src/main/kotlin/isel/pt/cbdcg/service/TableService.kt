package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.TableError
import isel.pt.cbdcg.repository.UserError
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
     * @throws TableError.DuplicateName Table names must be unique.
     * @throws UserError.EmailNotFound No user with the provided email exists.
     */
    fun createTable(name: Name, owner: Email): Result<Table> = runCatching {
        if(tableRepo.findByName(name) != null)
            throw TableError.DuplicateName(name.string)

        val owner = userRepo.findByEmail(owner)
            ?: throw UserError.EmailNotFound(owner.string)

        val res = tableRepo.createTable(name, owner.id)
        participantRepo.joinTable(owner, res)

        res
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
            throw TableError.UserUnavailable(user.name, availability)

        participantRepo.joinTable(user, table)
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
            throw TableError.UserNotFound(user.name, table.name)

        participantRepo.leaveTable(user, table)
    }

    /**
     * Function to change a participant's role.
     * @param participant Email of the participant.
     * @param newRole The new role to assign.
     * @throws isel.pt.cbdcg.repository.ParticipantError.ParticipantEmailNotFound If participant is not found.
     */
    fun changeRole(participant: Email, newRole: Role) = runCatching {
        val participant = participantRepo.findByEmail(participant)
            ?: throw isel.pt.cbdcg.repository.ParticipantError.ParticipantEmailNotFound(participant)

        participantRepo.changeRole(participant, newRole)
    }
}