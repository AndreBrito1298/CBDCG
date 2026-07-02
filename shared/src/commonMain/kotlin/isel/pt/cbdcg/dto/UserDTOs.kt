package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class UserDTO(
    val id: Int,
    val name: String,
    val email: String,
    val password: String,
    val auth: AuthUserDTO?
)

@Serializable
data class AuthUserDTO(
    val token: String,
    val userId: Int,
    val gameId: Int?,
    val tokenRefresh: Long,
    val tokenExpiration: Long,
)

fun AuthUserDTO.toAuthUser(): AuthUser = AuthUser(
    token = token,
    userId = userId.toUInt(),
    gameId = gameId?.toUInt(),
    tokenRefresh = Instant.fromEpochMilliseconds(tokenRefresh),
    tokenExpiration = Instant.fromEpochMilliseconds(tokenExpiration),
)

fun UserDTO.toUser(): User = User(
    id = id.toUInt(),
    name = Name(name),
    email = Email(email),
    password = Password(password),
    auth = auth?.toAuthUser(),
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
