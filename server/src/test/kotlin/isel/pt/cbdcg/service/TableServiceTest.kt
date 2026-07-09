package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.webapi.websocket.EventsPublisher
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeEventsPublisher : EventsPublisher {
    val lobbyEvents = mutableListOf<List<Table>>()
    val tableEvents = mutableListOf<Table>()
    val deletedTables = mutableListOf<Table>()
    val startedGames = mutableListOf<Game>()
    val gameEvents = mutableListOf<Game>()

    override suspend fun publishLobbyTables(tables: List<Table>) {
        lobbyEvents += tables
    }

    override suspend fun publishTableUpdated(table: Table) {
        tableEvents += table
    }

    override suspend fun publishGameStarted(table: Table, game: Game) {
        startedGames += game
    }

    override suspend fun publishTableDeleted(table: Table) {
        deletedTables += table
    }

    override suspend fun publishGameUpdated(game: Game) {
        gameEvents += game
    }

    fun clear() {
        lobbyEvents.clear()
        tableEvents.clear()
        deletedTables.clear()
        startedGames.clear()
        gameEvents.clear()
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
    fun clearRepo() = runBlocking {
        userRepo.clear()
        tableRepo.clear()
        participantRepo.clear()
        events.clear()
    }

    private suspend fun createAuthenticatedUser(name: String, email: String, password: String = "secret1"): User =
        userService.createUser(Name(name), Email(email), Password(password)).getOrThrow()

    @Test
    fun `get tables returns current repository contents`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        tableService.createTable(Name("tableOne"), owner.id).getOrThrow()

        val result = tableService.getTables().getOrThrow()

        assertEquals(1, result.size)
        assertEquals("tableOne", result.single().name.string)
    }

    @Test
    fun `create table creates owner participant and publishes events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")

        val table = tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()

        assertEquals(owner.id, table.owner.id)
        assertEquals(listOf(Participant(owner, Role.PLAYER)), table.participants)
        assertEquals(table, tableRepo.findByName(Name("lobbyOne")))
        assertEquals(1, events.lobbyEvents.size)
        assertEquals(1, events.tableEvents.size)
    }

    @Test
    fun `create table fails when owner id does not exist`(): Unit = runBlocking {
        assertFailsWith<UserError.TokenNotFound> {
            tableService.createTable(Name("lobbyOne"), 9128u).getOrThrow()
        }
    }

    @Test
    fun `join table adds player and publishes events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()

        val updated = tableService.joinTable(guest.id, 0u).getOrThrow()

        assertEquals(2, updated.participants.size)
        assertEquals(Role.PLAYER, updated.participants.last().role)
        assertEquals(2, events.lobbyEvents.size)
        assertEquals(2, events.tableEvents.size)
    }

    @Test
    fun `join table adds spectator when table already has four participants`() = runBlocking {
        val users = listOf(
            createAuthenticatedUser("Alice", "alice@email.com"),
            createAuthenticatedUser("Bea", "bea@email.com"),
            createAuthenticatedUser("Cai", "cai@email.com"),
            createAuthenticatedUser("Dio", "dio@email.com"),
            createAuthenticatedUser("Eva", "eva@email.com"),
        )

        tableService.createTable(Name("lobbyOne"), users[0].id).getOrThrow()
        users.drop(1).take(3).forEach { tableService.joinTable(it.id, 0u).getOrThrow() }

        val updated = tableService.joinTable(users[4].id, 0u).getOrThrow()

        assertEquals(5, updated.participants.size)
        assertEquals(Role.SPECTATOR, updated.participants.last().role)
    }

    @Test
    fun `join table fails when table does not exist`(): Unit = runBlocking {
        val user = createAuthenticatedUser("Bea", "bea@email.com")

        assertFailsWith<TableError.TableDoesNotExist> {
            tableService.joinTable(user.id, 1999u).getOrThrow()
        }
    }

    @Test
    fun `join table fails when user is already on a table`(): Unit = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val user = createAuthenticatedUser("Bea", "bea@email.com")
        val otherOwner = createAuthenticatedUser("Cai", "cai@email.com")

        tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()
        tableService.createTable(Name("lobbyTwo"), otherOwner.id).getOrThrow()
        tableService.joinTable(user.id, 0u).getOrThrow()

        assertFailsWith<TableError.UserUnavailable> {
            tableService.joinTable(user.id, 1u).getOrThrow()
        }
    }

    @Test
    fun `leave table removes non owner and publishes table and lobby events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()
        tableService.joinTable(guest.id, 0u).getOrThrow()

        tableService.leaveTable(guest.id, 0u).getOrThrow()

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
        tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()
        tableService.joinTable(guest.id, 0u).getOrThrow()

        tableService.leaveTable(owner.id, 0u).getOrThrow()

        assertNull(tableRepo.findByName(Name("lobbyOne")))
        assertTrue(participantRepo.userAvailability(owner))
        assertTrue(participantRepo.userAvailability(guest))
        assertEquals(1, events.deletedTables.size)
    }

    @Test
    fun `leave table fails when user is not in table`(): Unit = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()

        assertFailsWith<TableError.UserNotFound> {
            tableService.leaveTable(guest.id, 0u).getOrThrow()
        }
    }

    @Test
    fun `change role updates participant role and publishes events`() = runBlocking {
        val owner = createAuthenticatedUser("Alice", "alice@email.com")
        val guest = createAuthenticatedUser("Bea", "bea@email.com")
        tableService.createTable(Name("lobbyOne"), owner.id).getOrThrow()
        tableService.joinTable(guest.id, 0u).getOrThrow()

        tableService.changeRole(guest.id, 0u, Role.READY).getOrThrow()

        val stored = tableRepo.findByName(Name("lobbyOne"))
        assertNotNull(stored)
        assertEquals(Role.READY, stored.participants.first { it.user.id == guest.id }.role)
        assertEquals(3, events.lobbyEvents.size)
        assertEquals(3, events.tableEvents.size)
    }
}
