package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object UserRepositoryDB: Repository<User> {

    override fun findById(id: UInt): User? {
        return transaction {
            Users.selectAll().where { Users.id eq id }
                .singleOrNull()
                ?.toUser()
        }
    }

    fun findByEmail(email: Email): User? {
        return transaction {
            Users.selectAll().where { Users.email eq email.string }
                .singleOrNull()
                ?.toUser()
        }
    }

    override fun save(element: User) {
        transaction {
            Users.insert {
                it[name] = element.name.string
                it[email] = element.email.string
                it[password] = element.password.string
                it[creationDate] = element.creationDate
                it[creationTime] = element.creationDate
            }
        }
    }

    override fun deleteById(id: UInt) {
        transaction {
            Users.deleteWhere { Users.id eq id }
        }
    }

    override fun clear() {
        transaction {
            Users.deleteAll()
        }
    }

    /**
     * Helper function to convert a ResultRow to a User domain object.
     */
    private fun ResultRow.toUser() = User(
        id = this[Users.id],
        name = Name(this[Users.name]),
        email = Email(this[Users.email]),
        password = Password(this[Users.password]),
        creationDate = this[Users.creationDate]
    )
}