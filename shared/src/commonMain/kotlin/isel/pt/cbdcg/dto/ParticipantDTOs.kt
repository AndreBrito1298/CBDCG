package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantDTO(
    val user: UserDTO,
    val role: String,
    val ready: Boolean
)

fun Participant.toParticipantDTO(): ParticipantDTO = ParticipantDTO(
    user = user.toUserDTO(),
    role = role.name,
    ready = ready
)

fun ParticipantDTO.toParticipant(): Participant = Participant(
    user = user.toUser(),
    role = Role.valueOf(role),
    ready = ready
)

