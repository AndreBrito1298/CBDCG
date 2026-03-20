package isel.pt.cbdcg.domain

/**
 * Class that represents the Email of a User.
 * @param string Email string provided by the user.
 */
@JvmInline
value class Email(
    val string: String,
) {
    private fun String.validate(): Boolean {
        val regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return this.length <= 255 && this.matches(regex)
    }

    /**
     * Any Email should have a format that matches the common regular expression.
     */
    init {
        require(string.validate()) { "Email format is invalid." }
    }
}

/**
 * Function to transform a String into an Email.
 */
fun String.toEmail(): Email = Email(this)