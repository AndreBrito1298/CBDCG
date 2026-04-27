package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.ParticipantRepository
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.ParticipantsDao
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object ParticipantRepositoryDB: ParticipantRepository {

    override fun createParticipant(
        user: User,
        role: Role
    ): Participant {
        return ParticipantsDao.new {

        }.toParticipant()
    }

    override fun userAvailability(user: User): Boolean {
        return transaction {
            ParticipantsDao.find { Participants.id eq user.id.toInt() }.empty()
        }
    }

    override fun deleteParticipant(user: User) {
        transaction {
            ParticipantsDao.find { Participants.id eq user.id.toInt() }
                .forEach { it.delete() }
        }
    }
}
