package isel.pt.cbdcg.error

/**
 * Base Error class used to restrict every specific Error Type created.
 * @param msg Custom error message passed to the RuntimeException.
 * @param desc Message with the 'Domain Class' where the error occurred.
 */
sealed class Error(
    msg: String,
    val desc: String
) : RuntimeException(msg)

/**
 * Base class related to every User-related operations.
 * @param msg Custom error message specifying the operation that failed.
 */
sealed class UserError(
    msg: String
) : Error(msg, "Error found during User related operations.") {

    class DuplicateEmail(
        email: String
    ) : UserError("Email '$email' is already in use.")

    class EmailNotFound(
        email: String
    ) : UserError("Email '$email' is not bound to any account.")

    class PasswordMismatch: UserError("Passwords do not match.")

    class TokenNotFound: UserError("Authentication token was not found.")

    class TokenMismatch: UserError("Token does not match.")

    class AlreadyLoggedIn: UserError("User is logged in on a different client.")

    class IdNotFound: UserError("ID does not exist.")

    class OAuthError(
        reason: String
    ) : UserError("OAuth authentication failed: $reason")
}

sealed class TableError(
    msg: String,
) : Error(msg, "Error found during Table related operations.") {

    class DuplicateName(
        name: String,
    ) : TableError("Name '$name' is already in use.")

    class UserUnavailable(name: String) :
        TableError("User '$name' is already on a different table.")

    class UserNotFound(name: String, table: String) :
            TableError("User '$name' is not found in table '$table'.")

    class TableDoesNotExist(table: String) :
            TableError("Table '$table' was not found.")

    class OwnerOnly :
            TableError("This operation can only be performed by the owner of the table.")

}

sealed class ParticipantError(
    msg: String,
): Error(msg, "Error found during table participant operations."){

    class ParticipantIdNotFound(id: UInt) : ParticipantError("Participant $id not found.")

    class ParticipantEmailNotFound(email: String) : ParticipantError("Participant $email not found.")

    class UserNotOnTable : ParticipantError("User is not participating in any table.")

}

sealed class BoardPlacementError(
    msg: String,
): Error(msg, "Error found when performing a Board Tile related operation."){

    class PositionTaken(x: Int, y: Int): BoardPlacementError("The position ($x,$y) is already taken.")

    class TileConnectionMismatch: BoardPlacementError("The tile does not connect to the rest of the board.")
}

sealed class GameError(
    msg: String,
): Error(msg, "Error found while playing the game."){

    class NotYourTurn: GameError("Wait for your turn.")
    class MinimumPlayersNeeded: GameError("There must be at least 2 players to start a game.")
    class EveryPlayerReady: GameError("Every Player must be ready to start a Game.")
    class InvalidDirection(char: Char): GameError("Something went wrong decoding direction: $char")
    class GameNotFound(id: Int): GameError("No game found with the id '$id'.")
    class PlayerNotFound(email: String, game: Int): GameError("Player with email '$email' was not found in game '$game'.")
    class CardDoesNotExist(string: String) : GameError("The Card '$string' could not be decoded.")
    class DungeonTurnZeroRule: GameError("You can only place Tiles.")
}