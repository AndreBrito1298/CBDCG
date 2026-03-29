package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import org.h2.command.Token
import java.util.UUID

class UserService(
    private val userRepo: UserRepositoryMem,
) {
    /**
     * Function to create a user.
     * @param name The name of the user.
     * @param email The email of the user (unique).
     * @param password The password of the user.
     * @throws UserError.DuplicateEmail The users email must be unique.
     */
    fun createUser(name: Name, email: Email, password: Password): Result<User> = runCatching {
        if(userRepo.findByEmail(email) != null)
            throw UserError.DuplicateEmail(email.string)

        userRepo.createUser(name, email, password)
    }

    /**
     * Function to login a user to the app.
     * @param email The email of the user.
     * @param password The password of the user.
     * @throws UserError.EmailNotFound No user in 'users' list had the email provided.
     * @throws UserError.PasswordMismatch The password provided does not correspond to the one recorded in the 'users' list.
     */
    fun login(email: Email, password: Password): Result<AuthUser> = runCatching {
        val user = userRepo.findByEmail(email)
            ?: throw UserError.EmailNotFound(email.string)
        if(user.password.string != password.string)
            throw UserError.PasswordMismatch()
        val token = UUID.randomUUID().toString()
        val authUser = AuthUser(user.email, token)
        userRepo.login(authUser)
        authUser
    }

    /**
     * Function to logout a user from the app.
     * @param email The email of the user.
     * @throws UserError.EmailNotFound No user in 'users' list had the email provided.
     */
    fun logout(email: Email) : Result<User> = runCatching {
        val user = userRepo.findByEmail(email)
            ?: throw UserError.EmailNotFound(email.string)
        userRepo.logout(email)
        user
    }

    /**
     * Function to handle OAuth login/registration.
     * If user exists, logs them in. If not, creates a new user and logs them in.
     * @param email The email obtained from OAuth provider.
     * @param name The name obtained from OAuth provider.
     * @return AuthUser containing the email and token.
     */
    fun loginOrRegisterWithOAuth(email: Email, name: Name, token: String): Result<AuthUser> = runCatching {
        val existingUser = userRepo.findByEmail(email)
        val user = if (existingUser != null) {
            existingUser
        } else {
            val oauthPassword = Password(UUID.randomUUID().toString())
            userRepo.createUser(name, email, oauthPassword)
        }
        val authUser = AuthUser(user.email, token)
        userRepo.login(authUser)
        authUser
    }

    /**
     * Function to find an authenticated user by token.
     * @param token The authentication token.
     * @return The AuthUser if found, null otherwise.
     */
    fun findByToken(token: String): AuthUser? {
        return userRepo.authenticatedUsers.find { it.token == token }
    }
}