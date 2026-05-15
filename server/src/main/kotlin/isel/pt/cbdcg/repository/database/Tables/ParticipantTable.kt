package isel.pt.cbdcg.repository.database.Tables

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.toRole
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object Participants : IntIdTable("participants") {
    val userId = integer("userId").references(Users.id)
    val lobbyId = integer("lobbyId").references(Tables.id)
    val role = varchar("role", 10)
}

class ParticipantsDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ParticipantsDao>(Participants)

    var userId by Participants.userId
    var lobbyId by Participants.lobbyId
    var role by Participants.role

    override fun toString(): String {
        return "Participant(id=$id, lobbyName=$lobbyId, role=$role)"
    }

    fun toParticipant() = Participant(
        user = UsersDao.findById(userId)?.toUser()!!,
        role = role.toRole() ?: Role.SPECTATOR
    )
}
