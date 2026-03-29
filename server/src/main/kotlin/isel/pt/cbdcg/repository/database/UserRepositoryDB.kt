package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.configs.AuthUsers
import isel.pt.cbdcg.configs.Users
import isel.pt.cbdcg.domain.AuthUser
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

    fun loginUser(authenticatedUser: AuthUser) {
        return transaction {
            AuthUsers.insert {
                it[token] = authenticatedUser.token
                it[userEmail] = authenticatedUser.userEmail.toString()
            }
        }
    }

    fun logoutUser(email: Email) {
        return transaction {
            AuthUsers.deleteWhere { AuthUsers.userEmail eq email.string }
        }
    }

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
    )

    private fun ResultRow.toAuthUser() = AuthUser(
        token = this[AuthUsers.token],
        userEmail = Email(this[AuthUsers.userEmail])
    )
}