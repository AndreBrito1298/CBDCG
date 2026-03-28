package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.UserError
import isel.pt.cbdcg.repository.memory.UserRepositoryMem

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
    fun login(email: Email, password: Password): Result<User> = runCatching {
        val user = userRepo.findByEmail(email)
            ?: throw UserError.EmailNotFound(email.string)

        if(user.password.string != password.string)
            throw UserError.PasswordMismatch()

        user
    }

    /**
     * Function to logout a user from the app.
     * @param email The email of the user.
     * @throws UserError.EmailNotFound No user in 'users' list had the email provided.
     */
    fun logout(email: Email) : Result<User> = runCatching {
        userRepo.findByEmail(email)
            ?: throw UserError.EmailNotFound(email.string)
    }
}