package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.UserRepository
import java.util.UUID

class UserService(
    private val userRepo: UserRepository,
) {

    fun createUser(name: Name, email: Email, password: Password): Result<User> = runCatching {

        if(userRepo.findByEmail(email) != null)
            throw UserError.DuplicateEmail(email.string)

        val encryptedPassword = Password(SimpleCrypto.encrypt(password.string))
        val user = userRepo.createUser(name, email, encryptedPassword)

        val plainToken = UUID.randomUUID().toString()
        val encryptedToken = SimpleCrypto.encrypt(plainToken)
        userRepo.save(user.copy(auth = AuthUser(encryptedToken)))

        user.copy(auth = AuthUser(plainToken))
    }

    fun login(email: Email, password: Password): Result<User> = runCatching {

        val user = userRepo.findByEmail(email)
            ?: throw UserError.EmailNotFound(email.string)

        if(user.auth != null)
            throw UserError.AlreadyLoggedIn()
        val decryptedPassword = SimpleCrypto.decrypt(user.password.string)
        if(password.string != decryptedPassword)
            throw UserError.PasswordMismatch()

        val plainToken = UUID.randomUUID().toString()
        val encryptedToken = SimpleCrypto.encrypt(plainToken)
        val auth = user.copy(auth = AuthUser(encryptedToken))

        userRepo.save(auth)

        auth.copy(auth = AuthUser(plainToken))
    }

    fun logout(token: String) = runCatching {
        val user = userRepo.findByToken(SimpleCrypto.encrypt(token))
            ?: throw UserError.TokenNotFound()

        userRepo.save(user.copy(auth = null))
    }

    /*
    fun loginOrRegisterWithOAuth(email: Email, name: Name, token: String): Result<AuthUser> = runCatching {
        val existingUser = userRepo.findByEmail(email)
        val user = if (existingUser != null) {
            existingUser
        } else {
            val oauthPassword = Password(UUID.randomUUID().toString())
            userRepo.createUser(name, email, oauthPassword)
        }
        val authUser = AuthUser(user.email, user.name, token)
        userRepo.login(authUser)
        authUser
    }
    */
}
