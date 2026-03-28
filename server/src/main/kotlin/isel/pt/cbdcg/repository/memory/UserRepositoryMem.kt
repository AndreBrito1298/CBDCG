package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository

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
     * @return The created user.
     */
    fun createUser(name: Name, email: Email, password: Password): User {
        val user = User(users.size.toUInt(), name, email, password)
        users.add(user)
        return user
    }

    /**
     * Function to find a User given its Email.
     * @param email The Email of the user.
     * @return The user with the desired email, or null if not found.
     */
    fun findByEmail(email: Email): User? {
       return users.find{ it.email.string == email.string }
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