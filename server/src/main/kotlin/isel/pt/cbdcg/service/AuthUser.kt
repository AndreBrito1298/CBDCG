package isel.pt.cbdcg.domain

import com.android.identity.util.UUID
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.service.SimpleCrypto.decrypt
import isel.pt.cbdcg.service.SimpleCrypto.encrypt
import kotlin.time.Clock
import kotlin.time.DurationUnit
import kotlin.time.Instant
import kotlin.time.toDuration

val sessionIncrements = 5000.toDuration(DurationUnit.MILLISECONDS)
fun String.verifyToken(user: User, currGame: UInt?, userRepository: UserRepository) {
    if(user.auth == null)
        throw UserError.TokenNotFound()
    var newToken = user.auth!!.token

    if(currGame != user.auth!!.gameId && decrypt(user.auth!!.token) != this)
        throw UserError.TokenMismatch()

    if(user.auth!!.tokenExpiration < Clock.System.now())
        newToken = UUID.randomUUID().toString()

    userRepository.save(user.copy(auth = user.auth!!.copy(token = encrypt(newToken), tokenExpiration = getNextTokenRefresh())))
}

fun User.addToGame(gameId: UInt, userRepository: UserRepository) {
    val newUser = copy(auth = auth?.copy(gameId = gameId))
    userRepository.save(newUser)
}
fun User.removeFromGame(userRepository: UserRepository) {
    val newUser = copy(auth = auth?.copy(gameId = null))
    userRepository.save(newUser)
}


fun getNextTokenRefresh(): Instant = Clock.System.now()+sessionIncrements