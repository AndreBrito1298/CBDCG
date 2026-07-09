package isel.pt.cbdcg.domain

import com.android.identity.util.UUID
import isel.pt.cbdcg.GAME_SESSION_TIME
import isel.pt.cbdcg.REFRESH_INCREMENT
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.service.SimpleCrypto.decrypt
import isel.pt.cbdcg.service.SimpleCrypto.encrypt
import kotlin.time.Clock
import kotlin.time.Instant

suspend fun String.verifyToken(user: User, currGame: UInt?, userRepository: UserRepository) {

    val authState = user.auth

    if(authState == null)
        throw UserError.TokenNotFound()
    else {

        var newToken = authState.token

        if(currGame != authState.gameId && decrypt(authState.token) != this)
            throw UserError.TokenMismatch()

        if(authState.tokenExpiration< Clock.System.now()){
            userRepository.removeAuthentication(user.id)
            throw UserError.SessionExpired()
        }

        if(authState.tokenRefresh < Clock.System.now())
            newToken = UUID.randomUUID().toString()

        userRepository.save(user.copy(auth = authState.copy(token = encrypt(newToken), tokenRefresh = getNextTokenRefresh())))
    }
}

suspend fun User.addToGame(gameId: UInt, userRepository: UserRepository) {
    val newUser = copy(auth = auth?.copy(gameId = gameId, tokenExpiration = getGameSessionTime()))
    userRepository.save(newUser)
}

suspend fun User.removeFromGame(userRepository: UserRepository) {
    val newUser = copy(auth = auth?.copy(gameId = null))
    userRepository.save(newUser)
}


fun getNextTokenRefresh(): Instant = Clock.System.now()+REFRESH_INCREMENT
fun getGameSessionTime(): Instant = Clock.System.now()+GAME_SESSION_TIME