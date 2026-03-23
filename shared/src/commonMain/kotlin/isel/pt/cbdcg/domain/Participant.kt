package isel.pt.cbdcg.domain

/**
 * Class that associates a user with a table.
 * @param user Unique email of the associated user.
 * @param table Unique name of the associated table.
 * @param role Role of the user in the table (either Player or Spectator).
 */
data class Participant(
    val user: Email,
    val table: Name,
    val role: Role
)