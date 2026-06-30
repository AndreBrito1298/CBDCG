package isel.pt.cbdcg.repository.database.Tables

import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import kotlin.time.Instant

object Users : IntIdTable("users") {
    val name = varchar("name", MAX_NAME_LENGTH)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", MAX_NAME_LENGTH)
    val creationDate = long("creation_date")
}
class UsersDao(id: EntityID<Int>) : IntEntity(id){
    companion object : IntEntityClass<UsersDao>(Users)

    var name by Users.name
    var email by Users.email
    var password by Users.password
    var creationDate by Users.creationDate

    override fun toString():String {
        return "User(id=$id, name=$name, email=$email, password=$password, creation_date=$creationDate)"
    }

    fun toUser(): User {
        val auth = AuthUsersDao.find { AuthUsers.userId eq id.value }
            .singleOrNull()
            ?.toAuthUser()

        return User(
            id = id.value.toUInt(),
            name = Name(name),
            email = Email(email),
            password = Password(password),
            auth = auth,
        )
    }
}

object AuthUsers : IntIdTable("authUsers") {
    val token = varchar("token", 255).uniqueIndex()
    val userId = integer("user_id").references(Users.id).uniqueIndex()
    val gameId = integer("game_id").nullable()
    val tokenExpiration = long("token_expiration")
}

class AuthUsersDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthUsersDao>(AuthUsers)
    var token by AuthUsers.token
    var userId by AuthUsers.userId
    var gameId by AuthUsers.gameId
    var tokenExpiration by AuthUsers.tokenExpiration

    fun toAuthUser() = AuthUser(
        token = token,
        userId = userId.toUInt(),
        gameId = gameId?.toUInt(),
        tokenExpiration = Instant.fromEpochMilliseconds(tokenExpiration),
    )
}
