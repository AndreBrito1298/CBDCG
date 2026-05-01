package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.UserRepository

object UserRepositoryMem: UserRepository {

    /**
     * List of Users registered.
     */
    val users = mutableListOf<User>(
        User(0u, Name("Andre"), Email("a@gmail.com"), Password("teste")),
        User(1u, Name("Teste"), Email("t@gmail.com"), Password("teste")),
    )

    override fun findByEmail(email: Email): User? {
        return users.find{ it.email.string == email.string }
    }

    override fun createUser(name: Name, email: Email, password: Password): User {
        val user = User(users.size.toUInt(), name, email, password)
        users.add(user)
        return user
    }

    override fun findByToken(token: String): User? {
        return users.find{ it.auth != null && it.auth!!.token == token }
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