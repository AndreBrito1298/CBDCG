package isel.pt.cbdcg.repository.database.Tables

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.configs.MAX_EMAIL_LENGTH
import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = uinteger("id").autoIncrement()
    val name = varchar("name", MAX_NAME_LENGTH)
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", MAX_NAME_LENGTH)
    val creationDate = long("creation_date")
    val creationTime = long("creation_date")

    override val primaryKey = PrimaryKey(id)
}

object AuthUsers : Table("authUsers") {
    val token = varchar("token", 255)
    val userEmail = varchar("user_email", MAX_EMAIL_LENGTH).references(Users.email).uniqueIndex()

    override val primaryKey = PrimaryKey(userEmail)
}

data class UsersDTO(
    val id: Uint,
    val name: String,
    val email: String,
    val password: String,
    val creationDate: Long,
    val creationTime: Long,
)

data class AuthUsersDTO(
    val token: String,
    val userEmail: String
)