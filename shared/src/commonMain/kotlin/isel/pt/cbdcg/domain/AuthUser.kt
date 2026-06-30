package isel.pt.cbdcg.domain

import isel.pt.cbdcg.error.UserError
import kotlin.time.Instant

data class AuthUser(
    val token: String,
    val userId: UInt,
    val gameId: UInt?,
    val tokenExpiration: Instant,
)

fun String.verifyToken(user: User) {
    //thread acorda qnd o tempo max passa?
    if(user.auth == null)
        throw UserError.TokenNotFound()
    if(user.auth.token != this)
        throw UserError.TokenMismatch()
}