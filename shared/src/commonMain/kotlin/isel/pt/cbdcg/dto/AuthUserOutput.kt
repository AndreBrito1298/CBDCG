package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import kotlinx.serialization.Serializable

@Serializable
data class AuthUserOutput(
    val email: String,
    val token: String,
)

fun AuthUser.toAuthUserOutput(): AuthUserOutput = AuthUserOutput(
    token = token,
    email = userEmail.string,
)

fun AuthUserOutput.toAuthUser(): AuthUser = AuthUser(
    token = token,
    userEmail = Email(email),
)