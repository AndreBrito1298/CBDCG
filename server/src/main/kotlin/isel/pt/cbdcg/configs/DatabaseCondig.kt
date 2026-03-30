package isel.pt.cbdcg.configs

import org.jetbrains.exposed.sql.Table

object BDConfig {
    const val DB_NAME = "cbdcg"
    const val DB_URL = "jdbc:postgresql://localhost:5432/$DB_NAME"
    const val DB_USER = "postgres"
    const val DB_PASSWORD = ""
}

private const val MAX_CONNECTIONS = 100
private const val MAX_NAME_LENGTH = 20
private const val MAX_PASSWORD_LENGTH = 20
private const val MAX_EMAIL_LENGTH = 7

/**
 * Exposed table configuration for Users.
 */
// Deriva de IdTable
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

/**
 * Exposed table configuration for Game Tables.
 */
object Tables : Table("tables") {
    val id = uinteger("id").autoIncrement()
    val name = varchar("name", MAX_NAME_LENGTH).uniqueIndex()
    val owner = uinteger("owner").references(Users.id)
    val players = uinteger("capacity")

    override val primaryKey = PrimaryKey(id)
}

/**
 * Exposed table configuration for Participants.
 */
object Participants : Table("participants") {
    val userEmail = varchar("user_email", MAX_EMAIL_LENGTH).references(Users.email)
    val lobbyName = varchar("table_name", MAX_NAME_LENGTH).references(Tables.name)
    val role = varchar("role", 10)
    override val primaryKey = PrimaryKey(userEmail, lobbyName)
}