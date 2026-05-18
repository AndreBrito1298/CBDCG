package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDTO(
    val user: UserDTO,
    val role: String,
)

fun ParticipantDTO.toParticipant(): Participant = Participant(
    user = user.toUser(),
    role = Role.valueOf(role),
)

