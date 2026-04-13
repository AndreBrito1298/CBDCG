package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.repository.database.ParticipantRepositoryDB
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.toRole
import isel.pt.cbdcg.repository.ParticipantRepository
import isel.pt.cbdcg.repository.Repository
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.repository.database.Tables.Participants
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ParticipantRepositoryDB: ParticipantRepository {

    private fun ResultRow.toParticipant() = Participant(
        user = UserRepositoryDB.findById((this[Participants.id]))!!,
        role = this[Participants.role].toRole() ?: Role.SPECTATOR
    )

    override fun createParticipant(
        user: User,
        role: Role
    ): Participant {
        return Participant(user, role)
    }

    override fun userAvailability(user: User): Boolean {
        return transaction {
            Participants.selectAll().where { Participants.id eq user.id }.count() == 0L
        }
    }

    override fun deleteParticipant(user: User) {
        transaction {
            Participants.deleteWhere { id eq user.id }
        }
    }
}