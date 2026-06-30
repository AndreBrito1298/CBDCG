package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User

interface UserRepository: Repository<User> {

    suspend fun findByEmail(email: Email): User?

    suspend fun createUser(name: Name, email: Email, password: Password): User

    // fun login(user: User): User

    suspend fun findByToken(token: String): User?

    // fun logout(user: User)

}