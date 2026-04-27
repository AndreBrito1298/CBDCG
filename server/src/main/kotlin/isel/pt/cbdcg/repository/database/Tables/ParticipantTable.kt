package isel.pt.cbdcg.repository.database.Tables

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.configs.MAX_EMAIL_LENGTH
import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.toRole
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object Participants : IdTable<Int>("participants") {
    override val id = integer("id").references(Users.id).entityId()
    val userEmail = varchar("user_email", MAX_EMAIL_LENGTH).references(Users.email)
    val lobbyName = varchar("table_name", MAX_NAME_LENGTH).references(Tables.name)
    val role = varchar("role", 10)
    override val primaryKey = PrimaryKey(id)
}

class ParticipantsDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ParticipantsDao>(Participants)

    var userEmail by Participants.userEmail
    var lobbyName by Participants.lobbyName
    var role by Participants.role

    override fun toString(): String {
        return "Participant(id=$id, userEmail=$userEmail, lobbyName=$lobbyName, role=$role)"
    }

    fun toParticipant() = Participant(
        user = UsersDao.findById(id.value)?.toUser()!!,
        role = role.toRole() ?: Role.SPECTATOR
    )
}


