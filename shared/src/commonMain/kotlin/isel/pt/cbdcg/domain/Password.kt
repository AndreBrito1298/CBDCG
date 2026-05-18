package isel.pt.cbdcg.domain

import kotlin.jvm.JvmInline

/**
 * Class that represents a Password of the User.
 * @param string Password string provided by the user.
 */
@JvmInline
value class Password(
    val string: String,
) {

    /**
     * A valid password should have at least 5 non-blank characters.
     */
    init{
        require(string.isPasswordLengthValid()) { "Password must have at least 5 characters." }
    }

    override fun toString() = string
}

/**
 * Function to transform a String into a Password.
 */
fun String.toPassword(): Password = Password(this)

fun String.isPasswordLengthValid(): Boolean = this.isNotBlank() && this.length >= 5