package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDTO(
    val user: UserDTO,
    val role: String,
)

fun Participant.toParticipantDTO(): ParticipantDTO = ParticipantDTO(
    user = user.toUserDTO(),
    role = role.name,
)

fun ParticipantDTO.toParticipant(): Participant = Participant(
    user = user.toUser(),
    role = Role.valueOf(role),
)

