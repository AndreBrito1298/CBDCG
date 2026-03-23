package isel.pt.cbdcg.domain

import kotlin.jvm.JvmInline

/**
 * Class that represents the Username of the User, or the Name of a Table.
 * @param string Name string provided by the user.
 */
@JvmInline
value class Name(
    val string: String,
) {
    /**
     * A Name cannot be empty and must have a maximum of 20 characters.
     */
    init {
        require(string.isNameFilled()) { "Name is empty." }
        require(string.isNameLengthValid()) { "Name cannot have more than 20 characters." }
    }
}

/**
 * Function to transform a String into a Name.
 */
fun String.toName() = Name(this.trim())

fun String.isNameFilled(): Boolean = this.isNotBlank()
fun String.isNameLengthValid(): Boolean = this.trim().length <= 20