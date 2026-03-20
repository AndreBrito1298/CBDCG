package isel.pt.cbdcg.domain

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
        require(string.isNotBlank()) { "Password must not be blank." }
        require(string.length > 5) { "Password must have at least 5 characters." }
    }

}

/**
 * Function to transform a String into a Password.
 */
fun String.toPassword(): Password = Password(this)