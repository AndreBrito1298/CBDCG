package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.ParticipantError
import isel.pt.cbdcg.repository.Repository

object ParticipantRepositoryMem: Repository<Participant> {

    val participants = mutableListOf<Participant>()

    /**
     * Adds a user to a table as a participant.
     * No validation is performed here - validation should be done in the service layer.
     */
    fun joinTable(user: User, table: Table): Participant {
        val participant =
            Participant(
                user.email,
                table.name,
                Role.PLAYER
            )
        participants.add(participant)
        return participant
    }

    /**
     * Changes the role of a participant.
     */
    fun changeRole(participant: Participant, newRole: Role): Participant {
        val updatedP = participant.copy(role = newRole)
        participants.remove(participant)
        participants.add(updatedP)
        return updatedP
    }

    /**
     * Removes a user from a table.
     */
    fun leaveTable(user: User, table: Table) {
        participants.removeIf{ it.user == user.email && it.table == table.name }
    }

    /**
     * Returns the table name where the user is currently participating, or null if not in any table.
     */
    fun userAvailability(user: User): Name? {
        return participants.find{ it.user == user.email }?.table
    }

    /**
     * Checks if a user is in a specific table.
     */
    fun findUserInTable(user: User, table: Table): Boolean {
        return participants.any{ it.user == user.email && it.table == table.name }
    }

    /**
     * Finds a participant by email.
     * @return The participant, or null if not found.
     */
    fun findByEmail(email: Email): Participant? {
        return participants.find{ it.user == email}
    }

    // Generic Operations

    override fun findById(id: UInt): Participant? {
        val email = UserRepositoryMem.findById(id)?.email ?: return null
        return participants.find{ it.user == email}
    }

    override fun save(element: Participant) {
        participants.removeIf{ it.user == element.user}
        participants.add(element)
    }

    override fun deleteById(id: UInt) {
        val email = UserRepositoryMem.findById(id)?.email ?: return
        participants.removeIf{ it.user == email}
    }

    override fun clear() {
        participants.clear()
    }
}