package isel.pt.cbdcg.repository.database.Tables

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.configs.MAX_EMAIL_LENGTH
import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import org.jetbrains.exposed.sql.Table

object Participants : Table("participants") {
    val id = uinteger("id").references(Users.id)
    val userEmail = varchar("user_email", MAX_EMAIL_LENGTH).references(Users.email)
    val lobbyName = varchar("table_name", MAX_NAME_LENGTH).references(Tables.name)
    val role = varchar("role", 10)
    override val primaryKey = PrimaryKey(userEmail, lobbyName)
}

data class ParticipantDTO(
    val id: Uint,
    val userEmail: String,
    val lobbyName: String,
    val role: String
)
