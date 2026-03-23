package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Participant
import kotlinx.serialization.Serializable

@Serializable
data class ParticipantOutput(
    val user: String,
    val table: String,
    val role: String
)

fun Participant.toParticipantOutput(): ParticipantOutput = ParticipantOutput(
    user = user.string,
    table = table.string,
    role = role.name
)