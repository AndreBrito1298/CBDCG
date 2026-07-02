package isel.pt.cbdcg.domain

import isel.pt.cbdcg.dto.AuthUserDTO
import kotlin.time.Instant

data class AuthUser(
    val token: String,
    val userId: UInt,
    val gameId: UInt?,
    val tokenRefresh: Instant,
    val tokenExpiration: Instant,
)

fun AuthUser.toAuthUserDTO(): AuthUserDTO = AuthUserDTO(
    token = token,
    userId = userId.toInt(),
    gameId = gameId?.toInt(),
    tokenRefresh = tokenRefresh.toEpochMilliseconds(),
    tokenExpiration = tokenExpiration.toEpochMilliseconds(),
)
