package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.configs.Participants
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.toRole
import isel.pt.cbdcg.repository.Repository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object ParticipantRepositoryDB: Repository<Participant> {

    override fun findById(id: UInt): Participant? {
        TODO(
    //slight issue nesta ver q n ha id as of now best pois n ha id nesta
    //to be done seria ver o user e pesquisaar por essa id mas a função em si é desnecessária pra ja
    )

    }

    fun findByUserEmail(email: Email): List<Participant> {
        return transaction {
            Participants.selectAll().where { Participants.userEmail eq email.string }
                .map { it.toParticipant() }
        }
    }

    fun findByTableName(tableName: Name): List<Participant> {
        return transaction {
            Participants.selectAll().where { Participants.lobbyName eq tableName.string }
                .map { it.toParticipant() }
        }
    }

    fun joinTable(user: User, table: Table): Participant {
        val participant =
            Participant(
                user.email,
                table.name,
                Role.PLAYER
            )
        save(participant)
        return participant
    }

    override fun save(element: Participant) {
        transaction {
            Participants.insert {
                it[userEmail] = element.user.string
                it[lobbyName] = element.table.string
                it[role] = element.role.name
            }
        }
    }

    /**
     * Note: Since Participant has a composite key, this deletes by user email.
     * Consider using deleteByUserAndTable for more precise deletion.
     */
    override fun deleteById(id: UInt) {
        // Not directly applicable for composite key
        // You may want to implement a different deletion strategy
    }

    fun deleteByUserEmail(email: Email) {
        transaction {
            Participants.deleteWhere { userEmail eq email.string }
        }
    }

    fun deleteByTableName(tableName: Name) {
        transaction {
            Participants.deleteWhere { lobbyName eq tableName.string }
        }
    }

    override fun clear() {
        transaction {
            Participants.deleteAll()
        }
    }

    /**
     * Helper function to convert a ResultRow to a Participant domain object.
     */
    private fun ResultRow.toParticipant() = Participant(
        user = Email(this[Participants.userEmail]),
        table = Name(this[Participants.lobbyName]),
        role = this[Participants.role].toRole() ?: Role.SPECTATOR
    )
}