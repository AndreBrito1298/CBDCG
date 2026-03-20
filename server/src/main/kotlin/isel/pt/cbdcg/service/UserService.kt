package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.memory.UserRepositoryMem

class UserService(
    private val userRepo: UserRepositoryMem,
) {

    /**
     * Function to create a user.
     * @param name The name of the user.
     * @param email The email of the user (unique).
     * @param password The password of the user.
     */
    fun createUser(name: Name, email: Email, password: Password): Result<User> = runCatching {
        userRepo.createUser(name, email, password)
    }

    /**
     * Function to login a user to the app.
     * @param email The email of the user.
     * @param password The password of the user.
     */
    fun login(email: Email, password: Password): Result<User> = runCatching {
        userRepo.login(email, password)
    }
}