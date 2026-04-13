package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Table
import kotlinx.serialization.Serializable

@Serializable
data class TableDTO(
    val id: UInt,
    val name: String,
    val owner: UserDTO,
    val participants: Array<ParticipantDTO>,
) {
    // Funções geradas pelo InteliJ por causa de uma das propriedades da data class ser um 'Array'
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as TableDTO

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

@Serializable
data class TableOperationInput(
    val table: Int,
    val user: Int,
    val token: String
)

@Serializable
data class CreateTableDTO(
    val name: String,
    val userId: Int,
    val token: String,
)

fun Table.toTableDTO(): TableDTO = TableDTO(
    id = id,
    name = name.string,
    owner = owner.toUserDTO(),
    participants = participants.map{ it.toParticipantDTO() }.toTypedArray()
)

fun TableDTO.toTable(): Table = Table(
    id = id,
    name = Name(name),
    owner = owner.toUser(),
    participants = participants.map{ it.toParticipant() }
)
