package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class UserOutput(
    val id: UInt,
    val name: String,
    val email: String,
    val password: String,
)

fun User.toUserOutput(): UserOutput = UserOutput(
    id = id,
    name = name.string,
    email = email.string,
    password = password.string,
)