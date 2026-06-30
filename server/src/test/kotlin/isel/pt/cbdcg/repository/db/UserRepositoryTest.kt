package isel.pt.cbdcg.repository.db

import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Instant

class UserRepositoryTest {

    private val userRepo = UserRepositoryDB
    private val tableRepo = TableRepositoryDB

    @BeforeTest
    fun resetDb() {
        dbInit(reset = true)
    }

    private fun auth(userId: UInt, token: String = "token-$userId", gameId: UInt? = null) =
        AuthUser(
            token = token,
            userId = userId,
            gameId = gameId,
            tokenExpiration = Instant.fromEpochMilliseconds(1_000_000),
        )

    @Test
    fun `create user stores user and can find by id and email`() = runBlocking {
        val created = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("secret1"))

        assertEquals(created, userRepo.findById(created.id))
        assertEquals(created, userRepo.findByEmail(created.email))
    }

    @Test
    fun `find by email returns null when user does not exist`() = runBlocking {
        assertNull(userRepo.findByEmail(Email("missing@email.com")))
    }

    @Test
    fun `save inserts explicit user id`() = runBlocking {
        val user = isel.pt.cbdcg.domain.User(42u, Name("Alice"), Email("alice@email.com"), Password("secret1"))

        userRepo.save(user)

        assertEquals(user, userRepo.findById(42u))
    }

    @Test
    fun `save updates existing user fields`() = runBlocking {
        val created = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("secret1"))
        val updated = created.copy(name = Name("Alicia"), password = Password("secret2"))

        userRepo.save(updated)

        assertEquals(updated, userRepo.findById(created.id))
    }

    @Test
    fun `save persists auth user fields`() = runBlocking {
        val created = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("secret1"))
        val authenticated = created.copy(auth = auth(created.id, token = "token-123", gameId = 7u))

        userRepo.save(authenticated)

        assertEquals(authenticated, userRepo.findById(created.id))
        assertEquals(authenticated, userRepo.findByToken("token-123"))
    }

    @Test
    fun `find by token returns null for unknown token`() = runBlocking {
        val created = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("secret1"))
        userRepo.save(created.copy(auth = auth(created.id, token = "token-123")))

        assertNull(userRepo.findByToken("missing-token"))
    }

    @Test
    fun `save with null auth removes auth row`() = runBlocking {
        val created = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("secret1"))
        userRepo.save(created.copy(auth = auth(created.id, token = "token-123")))

        userRepo.save(created.copy(auth = null))

        assertNull(userRepo.findByToken("token-123"))
        assertNull(userRepo.findById(created.id)?.auth)
    }

    @Test
    fun `delete by id removes user auth and participant row`() = runBlocking {
        val owner = userRepo.createUser(Name("Owner"), Email("owner@email.com"), Password("secret1"))
        val guest = userRepo.createUser(Name("Guest"), Email("guest@email.com"), Password("secret1"))
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        tableRepo.updateParticipants(table, Participant(guest, Role.SPECTATOR))
        userRepo.save(guest.copy(auth = auth(guest.id, token = "guest-token")))

        userRepo.deleteById(guest.id)

        assertNull(userRepo.findById(guest.id))
        assertNull(userRepo.findByToken("guest-token"))
        assertEquals(listOf(Participant(owner, Role.PLAYER)), tableRepo.findById(table.id)?.participants)
    }

    @Test
    fun `delete by id removes owned tables before deleting owner`() = runBlocking {
        val owner = userRepo.createUser(Name("Owner"), Email("owner@email.com"), Password("secret1"))
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))

        userRepo.deleteById(owner.id)

        assertNull(userRepo.findById(owner.id))
        assertNull(tableRepo.findById(table.id))
    }

    @Test
    fun `clear removes users auth tables and participants`() = runBlocking {
        val owner = userRepo.createUser(Name("Owner"), Email("owner@email.com"), Password("secret1"))
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        userRepo.save(owner.copy(auth = auth(owner.id, token = "owner-token")))

        userRepo.clear()

        assertNull(userRepo.findById(owner.id))
        assertNull(userRepo.findByToken("owner-token"))
        assertNull(tableRepo.findById(table.id))
    }

    @Test
    fun `ids grow after create user`() = runBlocking {
        val first = userRepo.createUser(Name("One"), Email("one@email.com"), Password("secret1"))
        val second = userRepo.createUser(Name("Two"), Email("two@email.com"), Password("secret2"))

        assertNotNull(first)
        assertNotNull(second)
        assertEquals(first.id + 1u, second.id)
    }
}
