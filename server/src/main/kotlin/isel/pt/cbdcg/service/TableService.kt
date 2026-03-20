package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.TableError
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
    fun createTable(name: Name, owner: Email): Result<Table> = runCatching {

        val owner = userRepo.findByEmail(owner)
        val res = tableRepo.createTable(name, owner.id)

        participantRepo.joinTable(owner, res)

        res
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

    }

}