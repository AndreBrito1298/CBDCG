package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Table
import kotlinx.serialization.Serializable

@Serializable
data class TableOutput(
    val id: UInt,
    val name: String,
    val owner: UserOutput,
    val participants: Array<ParticipantOutput>,
) {
    // Funções geradas pelo InteliJ por causa de uma das propriedades da data class ser um 'Array'
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TableOutput

        if (id != other.id) return false
        if (name != other.name) return false
        if (owner != other.owner) return false
        if (!participants.contentEquals(other.participants)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + owner.hashCode()
        result = 31 * result + participants.contentHashCode()
        return result
    }
}

fun Table.toTableOutput(): TableOutput = TableOutput(
    id = id,
    name = name.string,
    owner = owner.toUserOutput(),
    participants = participants.map{ it.toParticipantOutput() }.toTypedArray()
)

fun TableOutput.toTable(): Table = Table(
    id = id,
    name = Name(name),
    owner = owner.toUser(),
    participants = participants.map{ it.toParticipant() }
)