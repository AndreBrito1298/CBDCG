package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.events.TableEventsPublisher
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeEventsPublisher : TableEventsPublisher {
    val lobbyEvents = mutableListOf<List<Table>>()
    val tableEvents = mutableListOf<Table>()

    override suspend fun publishLobbyTables(tables: List<Table>) {
        lobbyEvents += tables
    }

    override suspend fun publishTableUpdated(table: Table) {
        tableEvents += table
    }
}

class TableServiceTest {

    private val userRepo = UserRepositoryMem
    private val tableRepo = TableRepositoryMem
    private val participantRepo = ParticipantRepositoryMem
    private val events = FakeEventsPublisher()
    private val userService = UserService(userRepo)
    private val tableService = TableService(userRepo, tableRepo, participantRepo, events)

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
        tableRepo.clear()
        participantRepo.participants.clear()
        events.lobbyEvents.clear()
        events.tableEvents.clear()
    }

    private fun createAuthenticatedUser(name: String, email: String, password: String = "secret1") =
        userService.createUser(Name(name), Email(email), Password(password)).getOrThrow()

    @Test
    fun `get tables returns current repository contents`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        tableService.createTable(Name("tableOne"), owner.email, owner.auth!!.token).getOrThrow()

        val result = tableService.getTables().getOrThrow()

        assertEquals(1, result.size)
        assertEquals("tableOne", result.single().name.string)
    }

    @Test
    fun `create table successfully creates owner participant and publishes events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")

        val table = tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()

        assertEquals(owner, table.owner)
        assertEquals(1, table.participants.size)
        assertEquals(Role.PLAYER, table.participants.single().role)
        assertEquals(table, tableRepo.findByName(Name("lobbyOne")))
        assertEquals(1, events.lobbyEvents.size)
        assertEquals(1, events.tableEvents.size)
    }

    @Test
    fun `create table fails when owner email does not exist`(): Unit = runBlocking {
        assertFailsWith<UserError.EmailNotFound> {
            tableService.createTable(Name("lobbyOne"), Email("missing@email.com"), "token").getOrThrow()
        }
    }

    @Test
    fun `create table fails when user has no token`(): Unit = runBlocking {
        val owner = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("secret1"))

        assertFailsWith<UserError.TokenNotFound> {
            tableService.createTable(Name("lobbyOne"), owner.email, "token").getOrThrow()
        }
    }

    @Test
    fun `create table fails when token does not match user`(): Unit = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")

        assertFailsWith<UserError.TokenMismatch> {
            tableService.createTable(Name("lobbyOne"), owner.email, "wrong-token").getOrThrow()
        }
    }

    @Test
    fun `join table successfully adds player and publishes events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()

        val updated = tableService.joinTable(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()

        assertEquals(2, updated.participants.size)
        assertEquals(Role.PLAYER, updated.participants.last().role)
        assertEquals(2, events.lobbyEvents.size)
        assertEquals(2, events.tableEvents.size)
    }

    @Test
    fun `join table adds spectator when table already has four participants`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val p2 = createAuthenticatedUser("Bea", "bea@email.com")
        val p3 = createAuthenticatedUser("Cai", "cai@email.com")
        val p4 = createAuthenticatedUser("Dio", "dio@email.com")
        val p5 = createAuthenticatedUser("Eva", "eva@email.com")

        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()
        tableService.joinTable(p2.email, Name("lobbyOne"), p2.auth!!.token).getOrThrow()
        tableService.joinTable(p3.email, Name("lobbyOne"), p3.auth!!.token).getOrThrow()
        tableService.joinTable(p4.email, Name("lobbyOne"), p4.auth!!.token).getOrThrow()

        val updated = tableService.joinTable(p5.email, Name("lobbyOne"), p5.auth!!.token).getOrThrow()

        assertEquals(5, updated.participants.size)
        assertEquals(Role.SPECTATOR, updated.participants.last().role)
    }

    @Test
    fun `join table fails when table does not exist`(): Unit = runBlocking {
        val user = createAuthenticatedUser("Bea", "bea@email.com")

        assertFailsWith<TableError.TableDoesNotExist> {
            tableService.joinTable(user.email, Name("missing"), user.auth!!.token).getOrThrow()
        }
    }

    @Test
    fun `join table fails when user is already on a table`(): Unit = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val user = createAuthenticatedUser("Bea", "bea@email.com")
        val otherOwner = createAuthenticatedUser("Cai", "cai@email.com")

        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()
        tableService.createTable(Name("lobbyTwo"), otherOwner.email, otherOwner.auth!!.token).getOrThrow()
        tableService.joinTable(user.email, Name("lobbyOne"), user.auth!!.token).getOrThrow()

        assertFailsWith<TableError.UserUnavailable> {
            tableService.joinTable(user.email, Name("lobbyTwo"), user.auth!!.token).getOrThrow()
        }
    }

    @Test
    fun `leave table removes non owner from table and participant repository`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()
        tableService.joinTable(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()

        tableService.leaveTable(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()

        val stored = tableRepo.findByName(Name("lobbyOne"))
        assertNotNull(stored)
        assertEquals(1, stored.participants.size)
        assertTrue(participantRepo.userAvailability(guest))
        assertEquals(3, events.lobbyEvents.size)
        assertEquals(3, events.tableEvents.size)
    }

    @Test
    fun `leave table by owner deletes entire table`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()
        tableService.joinTable(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()

        tableService.leaveTable(owner.email, Name("lobbyOne"), owner.auth!!.token).getOrThrow()

        assertNull(tableRepo.findByName(Name("lobbyOne")))
        assertTrue(participantRepo.userAvailability(owner))
        assertTrue(participantRepo.userAvailability(guest))
    }

    @Test
    fun `leave table fails when user is not in table`(): Unit = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()

        assertFailsWith<TableError.UserNotFound> {
            tableService.leaveTable(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()
        }
    }

    @Test
    fun `change role toggles participant role and publishes events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.email, owner.auth!!.token).getOrThrow()
        tableService.joinTable(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()

        tableService.changeRole(guest.email, Name("lobbyOne"), guest.auth!!.token).getOrThrow()

        val stored = tableRepo.findByName(Name("lobbyOne"))
        assertNotNull(stored)
        assertEquals(Role.SPECTATOR, stored.participants.first { it.user == guest }.role)
        assertEquals(3, events.lobbyEvents.size)
        assertEquals(3, events.tableEvents.size)
    }
}
