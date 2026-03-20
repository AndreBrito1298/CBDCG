package isel.pt.cbdcg.domain

/**
 * Class that associates a user with a table.
 * @param id Unique identifier of the participant.
 * @param user Unique identifier of the associated user.
 * @param table Unique name of the associated table.
 * @param role Role of the user in the table (either Player or Spectator).
 */
data class Participant(
    val id: Int,
    val user: Int,
    val table: Name,
    val role: Role
)