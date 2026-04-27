package isel.pt.cbdcg.domain

import isel.pt.cbdcg.error.UserError

data class AuthUser (
    val token: String,
    val email: Email,
    val name: Name
)

fun String.verifyToken(user: User) {

    if(user.auth == null)
        throw UserError.TokenNotFound()

    if(user.auth.token != this)
        throw UserError.TokenMismatch()

}