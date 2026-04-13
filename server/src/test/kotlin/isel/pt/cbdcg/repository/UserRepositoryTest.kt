package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {

    private val userRepo = UserRepositoryMem

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
    }

    @Test
    fun `create user stores user`() {
        val user = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        assertEquals(user, userRepo.findById(user.id))
        assertEquals(user, userRepo.findByEmail(user.email))
    }

    @Test
    fun `save replaces existing user by id`() {
        val user = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
        val authenticated = user.copy(auth = AuthUser("token-123"))

        userRepo.save(authenticated)

        assertEquals(authenticated, userRepo.findById(user.id))
        assertEquals(1, userRepo.users.count { it.id == user.id })
    }

    @Test
    fun `find by token returns authenticated user`() {
        val user = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
        val authenticated = user.copy(auth = AuthUser("token-123"))
        userRepo.save(authenticated)

        val found = userRepo.findByToken("token-123")

        assertEquals(authenticated, found)
    }

    @Test
    fun `find by token returns null for unknown token`() {
        val user = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
        userRepo.save(user.copy(auth = AuthUser("token-123")))

        assertNull(userRepo.findByToken("missing-token"))
    }

    @Test
    fun `delete by id removes user`() {
        val user = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        userRepo.deleteById(user.id)

        assertNull(userRepo.findById(user.id))
    }

    @Test
    fun `ids grow with repository size`() {
        val first = userRepo.createUser(Name("one"), Email("one@gmail.com"), Password("secret1"))
        val second = userRepo.createUser(Name("two"), Email("two@gmail.com"), Password("secret2"))

        assertNotNull(first)
        assertEquals(0u, first.id)
        assertEquals(1u, second.id)
    }
}
