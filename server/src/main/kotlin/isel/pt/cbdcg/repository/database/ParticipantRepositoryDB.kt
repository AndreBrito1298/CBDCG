package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.ParticipantRepository
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.ParticipantsDao
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

object ParticipantRepositoryDB: ParticipantRepository {

    override suspend fun createParticipant(
        user: User,
        table: Table,
        role: Role
    ): Participant {
        return suspendTransaction {
            val participant = ParticipantsDao.find { Participants.userId eq user.id.toInt() }
                .singleOrNull()
                ?: ParticipantsDao.new(user.id.toInt()) {
                    this.userId = user.id.toInt()
                    this.lobbyId = table.id.toInt()
                    this.role = role.name
                }

            participant.userId = user.id.toInt()
            participant.lobbyId = table.id.toInt()
            participant.role = role.name
            participant.toParticipant()
        }
    }

    override suspend fun userAvailability(user: User): Boolean {
        return suspendTransaction {
            ParticipantsDao.find { Participants.userId eq user.id.toInt() }.empty()
        }
    }

    override suspend fun deleteParticipant(user: User) {
        suspendTransaction {
            ParticipantsDao.find { Participants.userId eq user.id.toInt() }
                .forEach { it.delete() }
        }
    }

    override suspend fun findById(id: UInt): Participant? {
        return suspendTransaction {
            ParticipantsDao.find { Participants.userId eq id.toInt() }
                .singleOrNull()
                ?.toParticipant()
        }
    }

    override suspend fun save(element: Participant) {
        suspendTransaction {
            val existing = ParticipantsDao.find { Participants.userId eq element.user.id.toInt() }
                .singleOrNull()
                ?: return@suspendTransaction

            existing.role = element.role.name
        }
    }

    override suspend fun deleteById(id: UInt) {
        suspendTransaction {
            ParticipantsDao.find { Participants.userId eq id.toInt() }
                .forEach { it.delete() }
        }
    }

    override suspend fun clear() {
        suspendTransaction {
            ParticipantsDao.all().forEach { it.delete() }
        }
    }
}
