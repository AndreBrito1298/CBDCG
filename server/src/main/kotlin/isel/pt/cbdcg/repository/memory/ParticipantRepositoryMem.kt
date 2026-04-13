package isel.pt.cbdcg.repository.memory


import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.ParticipantRepository

object ParticipantRepositoryMem: ParticipantRepository {

    val participants = mutableListOf<Participant>()


    override fun createParticipant(user: User, role: Role): Participant {

        val participant = Participant(user, role)
        participants.add(participant)

        return participant
    }

    override fun userAvailability(user: User): Boolean {
        return participants.find{ it.user == user } == null
    }

    override fun deleteParticipant(user: User) {
        participants.removeIf{ it.user == user }
    }

    fun clear() {
        participants.clear()
    }

}