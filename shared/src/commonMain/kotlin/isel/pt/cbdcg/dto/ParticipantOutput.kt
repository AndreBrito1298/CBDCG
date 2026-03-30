package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantOutput(
    val user: UserOutput,
    val role: String
)

fun Participant.toParticipantOutput(): ParticipantOutput = ParticipantOutput(
    user = user.toUserOutput(),
    role = role.name
)

fun ParticipantOutput.toParticipant(): Participant = Participant(
    user = user.toUser(),
    role = Role.valueOf(role)
)
