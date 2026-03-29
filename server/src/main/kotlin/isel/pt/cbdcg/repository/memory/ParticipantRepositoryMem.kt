package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.error.ParticipantError
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository
import isel.pt.cbdcg.error.TableError

object ParticipantRepositoryMem: Repository<Participant> {

    val participants = mutableListOf<Participant>()

    fun joinTable(user: User, table: Table): Participant {
        val availability = userAvailability(user)
        if(availability != null)
            throw TableError.UserUnavailable(user.name.string, availability.string)
        val role =
            if(table.checkAvailability()) Role.PLAYER
            else Role.SPECTATOR

        val participant =
            Participant(
                user.email,
                table.name,
                role
            )
        participants.add(participant)
        return participant
    }

    fun changeRole(participant: Participant, newRole: Role): Participant {
        val p = participants.find { it.user == participant.user }?: throw ParticipantError.UserNotOnTable()
        val updatedP = p.copy(role = newRole)
        participants.remove(p)
        participants.add(updatedP)
        return updatedP
    }

    fun leaveTable(user: User, table: Table) {

        if(!findUserInTable(user, table))
            throw TableError.UserNotFound(user.name.string, table.name.string)

        participants.removeIf{ it.user == user.email && it.table == table.name }
    }

    fun userAvailability(user: User): Name? {
        return participants.find{ it.user == user.email }?.table
    }

    fun findUserInTable(user: User, table: Table): Boolean {
        return participants.any{ it.user == user.email && it.table == table.name }
    }

    fun findByEmail(email: Email): Participant{
        return participants.find{ it.user == email}?: throw ParticipantError.ParticipantEmailNotFound(email.string)
    }

    fun findByTable(table: Name): List<Participant> {
        return participants.filter { it.table == table }
    }

    fun deleteByTable(table: Name) {
        participants.removeIf { it.table == table }
    }


    // Generic Operations

    override fun findById(id: UInt): Participant {
        val id = UserRepositoryMem.findById(id)?.email?: throw ParticipantError.ParticipantIdNotFound(id)
        return participants.find{ it.user == id}!!
    }

    override fun save(element: Participant) {
        participants.removeIf{ it.user == element.user}
        participants.add(element)
    }

    override fun deleteById(id: UInt) {
        val id = UserRepositoryMem.findById(id)?.email?: throw ParticipantError.ParticipantIdNotFound(id)
        participants.removeIf{ it.user == id}
    }

    override fun clear() {
        participants.clear()
    }
}
