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
import kotlin.test.Ignore
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
        val created = userRepo.createUser(u.name, u.email, u.password)
        val authenticated = created.copy(auth = AuthUser("token-123", created.email, created.name))

        userRepo.save(authenticated)

        assertEquals(authenticated.name, userRepo.findById(created.id)?.name)
        assertEquals(authenticated.email, userRepo.findById(created.id)?.email)
        assertEquals(authenticated.password, userRepo.findById(created.id)?.password)
    }

    @Ignore // UserRepositoryDB.findByToken is not implemented yet
    @Test
    fun `find by token returns authenticated user`() {
        val created = userRepo.createUser(u.name, u.email, u.password)
        val authenticated = created.copy(auth = AuthUser("token-123", created.email, created.name))
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
        val created = userRepo.createUser(u.name, u.email, u.password)

        userRepo.deleteById(created.id)

        assertNull(userRepo.findById(created.id))
    }

    @Test
    fun `ids grow with repository size`() {
        val u1 = userRepo.createUser(Name("one"), Email("one@gmail.com"), Password("secret1"))
        val u2 = userRepo.createUser(Name("two"), Email("two@gmail.com"), Password("secret2"))

        assertNotNull(u1)
        assertNotNull(u2)
        assertEquals(u1.id + 1u, u2.id)
    }
}
