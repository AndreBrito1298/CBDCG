package isel.pt.cbdcg.repository.memory


import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.ParticipantRepository

object ParticipantRepositoryMem: ParticipantRepository {

    val participants = mutableListOf<Participant>()

    override suspend fun createParticipant(
        user: User,
        table: Table,
        role: Role
    ): Participant {
        val participant = Participant(user, role)
        participants.add(participant)

        return participant
    }

    override suspend fun userAvailability(user: User): Boolean {
        return participants.find{ it.user.id == user.id } == null
    }

    override suspend fun deleteParticipant(user: User) {
        participants.removeIf{ it.user.id == user.id }
    }

    override suspend fun findById(id: UInt): Participant? =
        participants.find{ it.user.id == id }


    override suspend fun save(element: Participant) {
        participants.removeIf{ it.user.id == element.user.id }
        participants.add(element)
    }

    override suspend fun deleteById(id: UInt) {
        participants.removeIf{ it.user.id == id }
    }

    override suspend fun clear() {
        participants.clear()
    }

}