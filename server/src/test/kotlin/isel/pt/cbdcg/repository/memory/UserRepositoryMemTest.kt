package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryMemTest {

    private val userRepo = UserRepositoryMem

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
    }

    @Test
    fun `create user stores user and can find by email`() {
        val created = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        val found = userRepo.findByEmail(created.email)
        assertNotNull(found)
        assertEquals(created, found)
    }

    @Test
    fun `save replaces existing user by id`() {
        val created = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
        val authenticated = created.copy(auth = AuthUser("token-123", created.email, created.name))

        userRepo.save(authenticated)

        assertEquals(authenticated, userRepo.findById(created.id))
    }

    @Test
    fun `find by token returns authenticated user`() {
        val created = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
        val authenticated = created.copy(auth = AuthUser("token-123", created.email, created.name))
        userRepo.save(authenticated)

        assertEquals(authenticated, userRepo.findByToken("token-123"))
    }

    @Test
    fun `delete by id removes user`() {
        val created = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

        userRepo.deleteById(created.id)

        assertNull(userRepo.findById(created.id))
    }
}
