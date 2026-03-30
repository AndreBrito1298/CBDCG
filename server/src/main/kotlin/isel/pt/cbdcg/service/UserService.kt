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

        val user = userRepo.createUser(name, email, password)
        val token = UUID.randomUUID().toString()
        val auth = user.copy(auth = AuthUser(token))

        userRepo.save(auth) // userRepo.login(user)

        auth
    }

    fun login(email: Email, password: Password): Result<User> = runCatching {

        val user = userRepo.findByEmail(email)
            ?: throw UserError.EmailNotFound(email.string)

        if(user.auth != null)
            throw UserError.AlreadyLoggedIn()

        if(user.password.string != password.string)
            throw UserError.PasswordMismatch()

        val token = UUID.randomUUID().toString()
        val auth = user.copy(auth = AuthUser(token))

        userRepo.save(auth)

        auth
    }

    fun logout(token: String) = runCatching {

        val user = userRepo.findByToken(token)
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