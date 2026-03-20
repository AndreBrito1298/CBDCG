package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Name

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

}

sealed class TableError(
    msg: String,
) : Error(msg, "Error found during Table related operations.") {

    class DuplicateName(
        name: String,
    ) : TableError("Name '$name' is already in use.")

    class UserUnavailable(name: Name, table: Name) :
        TableError("User '${name.string}' is already on table '${table.string}'.")

    class UserNotFound(name: Name, table: Name) :
            TableError("User '${name.string}' is not found in table '${table.string}'.")

    class TableDoesNotExist(table: String) :
            TableError("Table '$table' was not found.")

}