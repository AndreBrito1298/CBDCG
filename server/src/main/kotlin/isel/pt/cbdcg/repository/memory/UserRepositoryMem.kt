package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.UserRepository
import kotlin.time.Clock

object UserRepositoryMem: UserRepository {

    /**
     * List of Users registered.
     */
    val users = mutableListOf<User>()

    override suspend fun findByEmail(email: Email): User? {
        return users.find{ it.email.string == email.string }
    }

    override suspend fun createUser(name: Name, email: Email, password: Password): User {
        val user = User(users.size.toUInt(), name, email, password)
        users.add(user)
        return user
    }

    override suspend fun findByToken(token: String): User? {
        return users.find{ user ->
            val auth = user.auth
            auth != null && auth.token == token
        }
    }

    override suspend fun deleteInactiveUsers() {
        val now = Clock.System.now()

        for (index in users.indices) {
            val user = users[index]
            if (user.auth?.tokenExpiration?.let { it <= now } == true) {
                users[index] = user.copy(auth = null)
            }
        }
    }

    override suspend fun removeAuthentication(userId: UInt) {
        val u = users.find { it.id == userId }?:throw UserError.IdNotFound()
        users.remove(u)
        users.add(u.copy(auth = null))
    }


    // Generic Operations

    override suspend fun findById(id: UInt): User? {
        return users.find{ it.id == id}
    }

    override suspend fun save(element: User) {
        users.removeIf{ it.id == element.id }
        users.add(element)
    }

    override suspend fun deleteById(id: UInt) {
        users.removeIf{ it.id == id}
    }

    override suspend fun clear() {
        users.clear()
    }
}
