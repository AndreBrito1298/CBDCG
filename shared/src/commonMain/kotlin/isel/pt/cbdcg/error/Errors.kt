package isel.pt.cbdcg.error

import isel.pt.cbdcg.MIN_PLAYERS_TO_START

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
    class DuplicateEmail(email: String) : UserError("Email '$email' is already in use.")
    class EmailNotFound(email: String) : UserError("Email '$email' is not bound to any account.")
    class PasswordMismatch: UserError("Passwords do not match.")
    class TokenNotFound: UserError("Authentication token was not found.")
    class TokenMismatch: UserError("Token does not match.")
    class AlreadyLoggedIn: UserError("User is logged in on a different client.")

    class NotLoggedIn: UserError("User is not logged in.")

    class SessionExpired: UserError("Session expired.")
    class IdNotFound: UserError("ID does not exist.")
    class OAuthError(reason: String) : UserError("OAuth authentication failed: $reason")
}

sealed class TableError(
    msg: String,
) : Error(msg, "Error found during Table related operations.") {
    class DuplicateName(name: String) : TableError("Name '$name' is already in use.")
    class UserUnavailable(name: String) : TableError("User '$name' is already on a different table.")
    class UserNotFound(name: String, table: String) : TableError("User '$name' is not found in table '$table'.")
    class TableDoesNotExist(table: String) : TableError("Table '$table' was not found.")
    class OwnerOnly : TableError("This operation can only be performed by the owner of the table.")
    class MinimumPlayersNeeded: TableError("There must be at least $MIN_PLAYERS_TO_START players to start a game.")
    class EveryPlayerReady: TableError("Every Player must be ready to start a Game.")
}

sealed class ParticipantError(
    msg: String,
): Error(msg, "Error found during table participant operations."){
    class ParticipantIdNotFound(id: UInt) : ParticipantError("Participant $id not found.")
    class ParticipantEmailNotFound(email: String) : ParticipantError("Participant $email not found.")
    class UserNotOnTable : ParticipantError("User is not participating in any table.")
}

sealed class BoardError(
    msg: String,
): Error(msg, "Error found when performing a Board Tile related operation."){
    class PositionTaken(x: Int, y: Int): BoardError("The position ($x,$y) is already taken.")
    class TileConnectionMismatch: BoardError("The tile does not connect to the rest of the board.")
    class TileNotFound(x: Int, y: Int) : BoardError("No tile found in position $x,$y.")
    class EmptyTile : BoardError("There is no Character in this tile.")
    class ApplyEffectOnYourCharacter : BoardError("You can only apply this effect in your current Character.")
    class TileOccupied : BoardError("This tile is occupied.")
    class CharacterLimitReached : BoardError("You can only have one character in play.")
    class NoTargetFound : BoardError("No available target found.")
    class EffectInCooldown(name: String, remaining: Int) : BoardError("The effect $name is in cooldown (Dungeon Turns until ready: $remaining)")
}

sealed class GameError(
    msg: String,
): Error(msg, "Error found while playing the game."){
    class NotYourTurn: GameError("Wait for your turn.")
    class InvalidFormat(parameter: String, input: String): GameError("Invalid $parameter: $input")
    class GameNotFound(id: Int): GameError("No game found with the id '$id'.")
    class PlayerNotFound(id: Int?, email: String?, game: Int): GameError("Player with id '$id' and/or email '$email' was not found in game '$game'.")
    class DungeonTurnZeroRule: GameError("You can only place Tiles.")
    class MustPlaceTile(max: Int) : GameError("You can have a maximum of $max tiles in your hand.")
    class NoActiveCharacters : GameError("You must have at least one active character.")
    class CharacterMovementRestriction : GameError("You can only move your character during the Movement Phase.")
    class EffectNotFound(effect: String) : GameError("Effect '$effect' not found.")
    class EmptyDeck : GameError("Deck is empty.")
    class CharacterPlacementRestriction : GameError("You can only place a Character during the Substitution Phase.")
    class ReplaceYourCharacter : GameError("You can only replace your current character.")
    class TilePlacementRestriction : GameError("You can only place a Tile during the Construction Phase.")
    class EquipItemRestriction : GameError("You can only equip/unequip an Item during the Substitution Phase.")
    class BattleNotConcluded : GameError("There can be happening only one battle at a time.")
    class NoBattleOngoing : GameError("There is no battle ongoing.")
    class MustSelectATarget : GameError("You must select a target.")
    class MoveToBattleRestriction : GameError("You can only move your character to a battle during the Movement Phase.")
    class ItemGradeTooHigh(character: String, item: String) : GameError("The item grade '$item' is higher than the character grade '$character'.")
}

sealed class CharacterError(
    msg: String,
): Error(msg, "Error found in Character-related operation."){
    class ItemCapacityLimit(n: Int) : CharacterError("You can have a maximum of $n items equipped.")
    class ItemDoesNotExist(idx: Int) : CharacterError("This Characters does not have an item in the position $idx.")
    class CharacterDoesNotExist(name: String) : CharacterError("Character '$name' does not exist or isn't playable.")
}

sealed class CardError(
    msg: String,
): Error(msg, "Error found in Card-related operation."){
    class InvalidCardFormat(message: String) : CardError("Invalid Card format: $message.")
}

sealed class BattleError(
    msg: String,
): Error(msg, "Error found during a Battle."){
    class PassiveCantActivateOutsideBattle: BattleError("PassiveCantActivateOutsideBattle")
    class CharacterNotFound(name: String) : BattleError("Character '$name' is not participating in the battle.")
    class InvalidAction(name: String) : BattleError("Invalid action '$name'.")
    class InvalidPhase(name: String) : BattleError("Invalid phase '$name'.")
    class ActionAlreadyQueued : BattleError("You already have an action queued for this character.")
    class ActionNotQueued : BattleError("You don't have an action queued for this character.")
    class CantLeaveBattle : BattleError("You can't leave the battle if you are directly involved.")
}