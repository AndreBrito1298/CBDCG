package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import kotlinx.serialization.Serializable

@Serializable
data class AuthUserOutput(
    val email: String,
    val token: String,
    val name: String
)

fun AuthUser.toAuthUserOutput(): AuthUserOutput = AuthUserOutput(
    token = token,
    email = email.string,
    name = name.string
)

fun AuthUserOutput.toAuthUser(): AuthUser = AuthUser(
    token = token,
    email = Email(email),
    name = Name(name)
)