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

val refreshIncrements = 5000.toDuration(DurationUnit.MILLISECONDS)
val gameSessionTime = 600000.toDuration(DurationUnit.MILLISECONDS)
suspend fun String.verifyToken(user: User, currGame: UInt?, userRepository: UserRepository) {
    if(user.auth == null)
        throw UserError.TokenNotFound()
    var newToken = user.auth!!.token

    if(currGame != user.auth!!.gameId && decrypt(user.auth!!.token) != this)
        throw UserError.TokenMismatch()

    if(user.auth!!.tokenExpiration< Clock.System.now()){
        userRepository.removeAuthentication(user.id)
        throw UserError.SessionExpired()
    }

    if(user.auth!!.tokenRefresh < Clock.System.now())
        newToken = UUID.randomUUID().toString()

    userRepository.save(user.copy(auth = user.auth!!.copy(token = encrypt(newToken), tokenRefresh = getNextTokenRefresh())))
}

fun String.hasSession(user: User, userRepository: UserRepository){
    if(user.auth == null)
        throw UserError.TokenNotFound()

    if(user.auth!!.tokenExpiration < Clock.System.now())
        throw UserError.TokenNotFound()
}

suspend fun User.addToGame(gameId: UInt, userRepository: UserRepository) {
    val newUser = copy(auth = auth?.copy(gameId = gameId, tokenExpiration = getGameSessionTime()))
    userRepository.save(newUser)
}

suspend fun User.removeFromGame(userRepository: UserRepository) {
    val newUser = copy(auth = auth?.copy(gameId = null))
    userRepository.save(newUser)
}


fun getNextTokenRefresh(): Instant = Clock.System.now()+refreshIncrements
fun getGameSessionTime(): Instant = Clock.System.now()+gameSessionTime