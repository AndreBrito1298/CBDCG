package isel.pt.cbdcg.domain

import isel.pt.cbdcg.MAX_PLAYERS_TO_START
import isel.pt.cbdcg.dto.TableDTO

/**
 * Class that represents a Game Table.
 * @param id Unique identifier of a table.
 * @param name Name of the table (unique).
 * @param owner User that created the Table.
 * @param participants Players waiting to play or spectate.
 */
data class Table(
    val id: UInt,
    val name: Name,
    val owner: User,
    val participants: List<Participant>
)

/**
 * Function to check if there are any available 'player' seats in a table.
 */
fun Table.checkAvailability(): Boolean = participants.size < MAX_PLAYERS_TO_START

fun Table.toTableDTO(): TableDTO = TableDTO(
    id = id.toInt(),
    name = name.string,
    owner = owner.toUserDTO(),
    participants = participants.map{ it.toParticipantDTO() }.toTypedArray()
)