package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class UserOutput(
    val id: UInt,
    val name: String,
    val email: String,
    val password: String,
    val auth: String
)

fun User.toUserOutput(): UserOutput = UserOutput(
    id = id,
    name = name.string,
    email = email.string,
    password = password.string,
    auth = auth?.token ?: ""
)

fun UserOutput.toUser(): User = User(
    id = id,
    name = Name(name),
    email = Email(email),
    password = Password(password),
    auth = AuthUser(auth, Email(email), Name(name))
)