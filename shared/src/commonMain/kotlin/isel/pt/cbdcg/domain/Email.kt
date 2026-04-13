package isel.pt.cbdcg.domain

import kotlin.jvm.JvmInline

/**
 * Class that represents the Email of a User.
 * @param string Email string provided by the user.
 */
@JvmInline
value class Email(
    val string: String,
) {

    /**
     * Any Email should have a format that matches the common regular expression.
     */
    init {
        require(string.isEmailValid()) { "Email format is invalid." }
        require(string.isEmailLengthValid()) { "Email is too long." }
    }

    override fun toString() = string
}

/**
 * Function to transform a String into an Email.
 */
fun String.toEmail(): Email = Email(this)

fun String.isEmailValid(): Boolean {
    val regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
    return this.matches(regex)
}

fun String.isEmailLengthValid(): Boolean {
    return this.length <= 255
}