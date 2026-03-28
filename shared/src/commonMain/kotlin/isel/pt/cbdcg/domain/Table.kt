package isel.pt.cbdcg.domain

/**
 * Class that represents a Game Table.
 * @param id Unique identifier of a table.
 * @param name Name of the table (unique).
 * @param owner Id of the user that created the Table.
 * @param players Number of players waiting.
 */
data class Table(
    val id: UInt,
    val name: Name,
    val owner: UInt,
    val players: UInt
) {

    /**
     * Function to check if there are any available 'player' seats in a table.
     */
    fun checkAvailability(): Boolean = players < 4u

}