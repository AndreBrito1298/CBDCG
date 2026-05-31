package isel.pt.cbdcg.repository.database.Tables

import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

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

    fun toUser() = User(
        id = id.value.toUInt(),
        name = Name(name),
        email = Email(email),
        password = Password(password),
    )
}

object AuthUsers : IntIdTable("authUsers") {
    val token = varchar("token", 255)
    val userId = integer("user_id").index()
}

class AuthUsersDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AuthUsersDao>(AuthUsers)

    var token by AuthUsers.token
    var userId by AuthUsers.userId

}
