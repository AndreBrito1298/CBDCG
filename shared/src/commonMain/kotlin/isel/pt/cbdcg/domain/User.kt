package isel.pt.cbdcg.domain

import isel.pt.cbdcg.dto.UserDTO


/**
 * User representation:
 * @property id Unique identifier of the user.
 * @property name Name of the user.
 * @property email Email of the user (Unique).
 * @property password Password of the user.
 * @property auth Authentication state of the user.
 */
data class User(
    val id: UInt,
    val name: Name,
    val email: Email,
    val password: Password,
    val auth: AuthUser? = null
)

fun User.toUserDTO(): UserDTO = UserDTO(
    id = id.toInt(),
    name = name.string,
    email = email.string,
    password = password.string,
    auth = auth?.token ?: ""
)