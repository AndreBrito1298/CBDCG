package isel.pt.cbdcg.repository.memory

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository
import isel.pt.cbdcg.repository.UserError

object UserRepositoryMem: Repository<User> {

    /**
     * List of Users registered.
     */
    val users = mutableListOf<User>()

    /**
     * Function to create a user in the 'users' list.
     * @param name The name of the user.
     * @param email The email of the user.
     * @param password The password associated with the user.
     * @throws UserError.DuplicateEmail The users email must be unique.
     * @return The created user.
     */
    fun createUser(name: Name, email: Email, password: Password): User {

        if(users.any{ it.email.string == email.string })
            throw UserError.DuplicateEmail(email.string)

        val user = User(users.size.toUInt(),name, email, password)
        users.add(user)

        return user

    }

    /**
     * Function to login a user to the app.
     * @param email The email of the user.
     * @param password The password associated with the user.
     * @throws UserError.EmailNotFound No user in 'users' list had the email provided.
     * @throws UserError.PasswordMismatch The password provided does not correspond to the one recorded in the 'users' list.
     * @return The user with the email provided.
     */
    fun login(email: Email, password: Password): User {

        val user = users.find{ it.email.string == email.string }
            ?: throw UserError.EmailNotFound(email.string)

        if(user.password.string != password.string)
            throw UserError.PasswordMismatch()

        return user
    }

    fun logout(email: Email): User {
        return users.find { it.email.string == email.string  }?:
        throw UserError.EmailNotFound(email.string)
    }

    /**
     * Function to find a User given its Email.
     * @param email The Email of the user.
     * @return The user with the desired email.
     * @throws UserError.EmailNotFound No user in 'users' list had the email provided.
     */
    fun findByEmail(email: Email): User {
       return users.find{ it.email.string == email.string }
           ?: throw UserError.EmailNotFound(email.string)
    }

    // Generic Operations

    override fun findById(id: UInt): User? {
        return users.find{ it.id == id}
    }

    override fun save(element: User) {
        users.removeIf{ it.id == element.id }
        users.add(element)
    }

    override fun deleteById(id: UInt) {
        users.removeIf{ it.id == id}
    }

    override fun clear() {
        users.clear()
    }

}