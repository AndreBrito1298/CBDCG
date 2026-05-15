package isel.pt.cbdcg.repository.db

import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.database.ParticipantRepositoryDB
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserRepositoryTest {

    private val userRepo = UserRepositoryDB
    private val tableRepo = TableRepositoryDB
    private val participantRepo = ParticipantRepositoryDB
    val u = User(1u, Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
    val authedUser = AuthUser("thisisatoken", u.email, u.name)

    @BeforeTest
    fun clearRepo() {
        dbInit()
        participantRepo.clear()
        tableRepo.clear()
        userRepo.clear()
    }

    @Test
    fun `create user stores user`() {
       userRepo.save(u)

        //assertEquals(u, userRepo.findById(u.id))
        assertEquals(u.name, userRepo.findByEmail(u.email)!!.name)
    }

    @Test
    fun `save replaces existing user by id`() {
        userRepo.save(u)
        val authenticated = u.copy(auth = AuthUser("token-123", u.email, u.name))

        userRepo.save(authenticated)

        assertEquals(authenticated, userRepo.findById(u.id))
       // assertEquals(1, userRepo.count { it.id == u.id })
    }

    @Test
    fun `find by token returns authenticated user`() {
        userRepo.save(u)
        val authenticated = u.copy(auth = AuthUser("token-123", u.email, u.name))
        userRepo.save(authenticated)

        val found = userRepo.findByToken("token-123")

        assertEquals(authenticated, found)
    }

    /*
    @Test
    fun `find by token returns null for unknown token`() {
        val user = userRepo.createUser(Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))
        userRepo.save(user.copy(auth = AuthUser("token-123", user.email, user.name)))

        assertNull(userRepo.findByToken("missing-token"))
    }
     */


    @Test
    fun `delete by id removes user`() {
        userRepo.save(u)

        userRepo.deleteById(u.id)

        assertNull(userRepo.findById(u.id))
    }

    @Test
    fun `ids grow with repository size`() {
        val u1 = User(1u, Name("one"), Email("one@gmail.com"), Password("secret1"))
        val u2 = User(1u, Name("two"), Email("two@gmail.com"), Password("secret2"))

        userRepo.save(u1)
        userRepo.save(u2)

        assertNotNull(u1)
        assertEquals(0u, u1.id)
        assertEquals(1u, u2.id)
    }
}
