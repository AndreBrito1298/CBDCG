package isel.pt.cbdcg.domain

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
) {

    /**
     * Function to check if there are any available 'player' seats in a table.
     */
    fun checkAvailability(): Boolean = participants.size < 4

}