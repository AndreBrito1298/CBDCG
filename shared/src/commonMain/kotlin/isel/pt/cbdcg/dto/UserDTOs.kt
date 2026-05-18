package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val auth: String
)

fun UserDTO.toUser(): User = User(
    id = id.toUInt(),
    name = Name(name),
    email = Email(email),
    password = Password(password),
    auth = AuthUser(auth)
)

@Serializable
data class LogoutInput(
    val token: String,
)

@Serializable
data class LoginInput(
    val email: String,
    val password: String,
)

@Serializable
data class CreateUserDTO(
    val name: String,
    val email: String,
    val password: String,
)