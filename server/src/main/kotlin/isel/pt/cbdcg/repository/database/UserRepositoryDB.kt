package isel.pt.cbdcg.repository.database

/*

import AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.repository.database.Tables.AuthUsers
import isel.pt.cbdcg.repository.database.Tables.AuthUsersDao
import isel.pt.cbdcg.repository.database.Tables.Users
import isel.pt.cbdcg.repository.database.Tables.UsersDao
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object UserRepositoryDB: UserRepository {

    fun loginUser(authenticatedUser: AuthUser) {
        transaction {
            val user = UsersDao.find { Users.id eq authenticatedUser.userId.toInt() }.singleOrNull()
                ?: return@transaction

            AuthUsersDao.new {
                token = authenticatedUser.token
                userId = user.id.value
            }
        }
    }

    fun logoutUser(email: Email) {
        transaction {
            val user = UsersDao.find { Users.email eq email.string }.singleOrNull()
                ?: return@transaction
            AuthUsersDao.find { AuthUsers.userId eq user.id.value }
                .forEach { it.delete() }
        }
    }

    fun updateSession(authenticatedUser: AuthUser) {
        transaction {
            val user= AuthUsersDao.find { AuthUsers.userId eq authenticatedUser.userId.toInt() }
            if(user != null) {
            }
        }
    }

    override fun findById(id: UInt): User? {
        return transaction {
            val user = UsersDao.findById(id.toInt()) ?: return@transaction null
            user.toUser()
        }
    }

    override fun findByEmail(email: Email): User? {
        return transaction {
            val user = UsersDao.find { Users.email eq email.string }.singleOrNull() ?: return@transaction null
            user.toUser()
        }
    }

    override fun createUser(name: Name, email: Email, password: Password): User {
        return transaction {
            val created = UsersDao.new {
                this.name = name.string
                this.email = email.string
                this.password = password.string
                this.creationDate = 0L
            }
            created.toUser()
        }
    }

    override fun save(element: User) {
        return transaction {
            val existing = UsersDao.findById(element.id.toInt())
            if (existing == null) {
                UsersDao.new {
                    name = element.name.string
                    email = element.email.string
                    password = element.password.string
                    creationDate = 0L
                }
            } else {
                existing.name = element.name.string
                existing.email = element.email.string
                existing.password = element.password.string
            }
        }
    }

    override fun findByToken(token: String): User? {
        TODO()
        return transaction {
            UsersDao.find { Users.email eq token }
                .singleOrNull()
                ?.toUser()
        }
    }

    override fun deleteById(id: UInt) {
        transaction {
            UsersDao.findById(id.toInt())?.delete()
        }
    }

    override fun clear() {
        transaction {
            AuthUsersDao.all().forEach { it.delete() }
            UsersDao.all().forEach { it.delete() }
        }
    }
}
*/