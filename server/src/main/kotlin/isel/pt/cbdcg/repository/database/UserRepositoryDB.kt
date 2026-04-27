package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository
import isel.pt.cbdcg.repository.database.Tables.AuthUsers
import isel.pt.cbdcg.repository.database.Tables.AuthUsersDao
import isel.pt.cbdcg.repository.database.Tables.Users
import isel.pt.cbdcg.repository.database.Tables.UsersDao
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object UserRepositoryDB: Repository<User> {

    fun loginUser(authenticatedUser: AuthUser) {
        transaction {
            val user = UsersDao.find { Users.email eq authenticatedUser.email.string }.singleOrNull()
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

    override fun findById(id: UInt): User? {
        return transaction {
            UsersDao.findById(id.toInt())?.toUser()
        }
    }

    fun findByEmail(email: Email): User? {
        return transaction {
            UsersDao.find { Users.email eq email.string }
                .singleOrNull()
                ?.toUser()
        }
    }

    override fun save(element: User) {
        transaction {
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
