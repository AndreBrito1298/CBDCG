package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository
import isel.pt.cbdcg.repository.TableError

object ParticipantRepositoryMem: Repository<Participant> {

    val participants = mutableListOf<Participant>()

    fun joinTable(user: User, table: Table): Participant {

        val availability = userAvailability(user)

        if(availability != null)
            throw TableError.UserUnavailable(user.name, availability)

        val role =
            if(table.checkAvailability()) Role.PLAYER
            else Role.SPECTATOR

        val participant =
            Participant(
                participants.size,
                user.id,
                table.name,
                role
            )

        participants.add(participant)
        return participant
    }

    fun leaveTable(user: User, table: Table) {

        if(!findUserInTable(user, table))
            throw TableError.UserNotFound(user.name, table.name)

        participants.removeIf{ it.user == user.id && it.table == table.name }
    }

    fun userAvailability(user: User): Name? {

        return participants.find{ it.user == user.id }?.table

    }

    fun findUserInTable(user: User, table: Table): Boolean {
        return participants.any{ it.user == user.id && it.table == table.name }
    }


    // Generic Operations

    override fun findById(id: Int): Participant? {
        return participants.find{ it.id == id}
    }

    override fun save(element: Participant) {
        participants.removeIf{ it.id == element.id}
        participants.add(element)
    }

    override fun deleteById(id: Int) {
        participants.removeIf{ it.id == id}
    }

    override fun clear() {
        participants.clear()
    }
}