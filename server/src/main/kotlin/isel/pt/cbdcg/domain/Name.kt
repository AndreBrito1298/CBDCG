package isel.pt.cbdcg.domain

/**
 * Class that represents the Username of the User, or the Name of a Table.
 * @param string Name string provided by the user.
 */
@JvmInline
value class Name(
    val string: String,
) {
    private fun String.validateName(): Boolean = this.isNotBlank() && this.trim().length <= 20

    /**
     * A Name cannot be empty and must have a maximum of 20 characters.
     */
    init {
        require(string.validateName()) { "Name is either empty or too long." }
    }
}

/**
 * Function to transform a String into a Name.
 */
fun String.toName() = Name(this.trim())
