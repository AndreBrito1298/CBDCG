package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Role
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

fun ParticipantOutput.toParticipant(): Participant = Participant(
    user = Email(user),
    table = Name(table),
    role = Role.valueOf(role)
)
