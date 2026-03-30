package isel.pt.cbdcg.domain

/**
 * Class that associates a user with a table.
 * @param user The User participating.
 * @param role Role of the user in the table (either Player or Spectator).
 */
data class Participant(
    val user: User,
    val role: Role
)