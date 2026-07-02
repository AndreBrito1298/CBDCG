package isel.pt.cbdcg.repository.database


import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.repository.database.Tables.AuthUsers
import isel.pt.cbdcg.repository.database.Tables.AuthUsersDao
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.ParticipantsDao
import isel.pt.cbdcg.repository.database.Tables.Tables
import isel.pt.cbdcg.repository.database.Tables.TablesDao
import isel.pt.cbdcg.repository.database.Tables.Users
import isel.pt.cbdcg.repository.database.Tables.UsersDao
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import kotlin.time.Clock

object UserRepositoryDB: UserRepository {
    override suspend fun findById(id: UInt): User? {
        return suspendTransaction {
            val user = UsersDao.findById(id.toInt())
            user?.toUser()
        }
    }

    override suspend fun findByEmail(email: Email): User? {
        return suspendTransaction {
            val user = UsersDao.find { Users.email eq email.string }.singleOrNull() ?: return@suspendTransaction null
            user.toUser()
        }
    }

    override suspend fun createUser(name: Name, email: Email, password: Password): User {
        return suspendTransaction {
            val created = UsersDao.new {
                this.name = name.string
                this.email = email.string
                this.password = password.string
                this.creationDate = 0L
            }
            created.toUser()
        }
    }

    override suspend fun save(element: User) {
        return suspendTransaction {
            val user = UsersDao.findById(element.id.toInt())
                ?: UsersDao.new(element.id.toInt()) {
                    name = element.name.string
                    email = element.email.string
                    password = element.password.string
                    creationDate = 0L
                }

            user.name = element.name.string
            user.email = element.email.string
            user.password = element.password.string

            AuthUsersDao.find { AuthUsers.userId eq element.id.toInt() }
                .forEach { it.delete() }

            element.auth?.let { auth ->
                AuthUsersDao.new {
                    token = auth.token
                    userId = element.id.toInt()
                    gameId = auth.gameId?.toInt()
                    tokenExpiration = auth.tokenExpiration.toEpochMilliseconds()
                }
            }
        }
    }

    override suspend fun findByToken(token: String): User? {
        return suspendTransaction {
            val au = AuthUsersDao.find { AuthUsers.token eq token }
                .singleOrNull()
            UsersDao.findById(au?.userId ?: return@suspendTransaction null)?.toUser()
        }
    }

    override suspend fun deleteInactiveUsers() {
        return suspendTransaction {
            AuthUsersDao.find { AuthUsers.tokenExpiration lessEq Clock.System.now().toEpochMilliseconds() }
                .forEach { it.delete() }
        }
    }

    override suspend fun removeAuthentication(userId: UInt) {
        suspendTransaction {
            AuthUsersDao.find { AuthUsers.userId eq userId.toInt() }.forEach { it.delete() }
        }
    }

    override suspend fun deleteById(id: UInt) {
        suspendTransaction {
            AuthUsersDao.find { AuthUsers.userId eq id.toInt() }
                .forEach { it.delete() }

            val ownedTables = TablesDao.find { Tables.owner eq id.toInt() }.toList()
            ownedTables.forEach { table ->
                ParticipantsDao.find { Participants.lobbyId eq table.id.value }
                    .forEach { it.delete() }
                table.delete()
            }

            ParticipantsDao.find { Participants.userId eq id.toInt() }
                .forEach { it.delete() }

            UsersDao.findById(id.toInt())?.delete()
        }
    }

    override suspend fun clear() {
        suspendTransaction {
            AuthUsersDao.all().forEach { it.delete() }
            ParticipantsDao.all().forEach { it.delete() }
            TablesDao.all().forEach { it.delete() }
            UsersDao.all().forEach { it.delete() }
        }
    }
}
