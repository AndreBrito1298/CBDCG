package isel.pt.cbdcg.repository.db

import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TableRepositoryTest {

    private val tableRepo = TableRepositoryDB
    private val userRepo = UserRepositoryDB

    private lateinit var owner: User
    private lateinit var guest: User
    private lateinit var spectator: User

    @BeforeTest
    fun resetDb() = runBlocking {
        dbInit(reset = true)
        owner = userRepo.createUser(Name("Owner"), Email("owner@email.com"), Password("secret1"))
        guest = userRepo.createUser(Name("Guest"), Email("guest@email.com"), Password("secret1"))
        spectator = userRepo.createUser(Name("Spectator"), Email("spectator@email.com"), Password("secret1"))
    }

    @Test
    fun `create table stores owner and first participant`() = runBlocking {
        val participant = Participant(owner, Role.PLAYER)

        val table = tableRepo.createTable(Name("Lobby"), owner, participant)

        assertEquals(table, tableRepo.findById(table.id))
        assertEquals(owner, table.owner)
        assertEquals(listOf(participant), table.participants)
        assertEquals(listOf(participant), tableRepo.getAllParticipants(table.id))
    }

    @Test
    fun `find by name returns stored table`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))

        assertEquals(table, tableRepo.findByName(Name("Lobby")))
    }

    @Test
    fun `find by id and name return null when table does not exist`() = runBlocking {
        assertNull(tableRepo.findById(999u))
        assertNull(tableRepo.findByName(Name("Missing")))
    }

    @Test
    fun `save inserts explicit table id`() = runBlocking {
        val table = Table(42u, Name("Lobby"), owner, listOf(Participant(owner, Role.PLAYER)))

        tableRepo.save(table)

        assertEquals(table, tableRepo.findById(42u))
    }

    @Test
    fun `save updates table fields and synchronizes participants`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        val updated = table.copy(
            name = Name("LobbyTwo"),
            participants = listOf(
                Participant(guest, Role.PLAYER),
                Participant(spectator, Role.SPECTATOR),
            ),
        )

        tableRepo.save(updated)

        assertEquals(updated, tableRepo.findById(table.id))
        assertEquals(null, tableRepo.findByName(Name("Lobby")))
        assertEquals(updated, tableRepo.findByName(Name("LobbyTwo")))
    }

    @Test
    fun `update participants adds participant for a new user`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))

        val updated = tableRepo.updateParticipants(table, Participant(guest, Role.SPECTATOR))

        assertEquals(
            listOf(Participant(owner, Role.PLAYER), Participant(guest, Role.SPECTATOR)),
            updated.participants,
        )
    }

    @Test
    fun `update participants replaces participant role for same user`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))

        val updated = tableRepo.updateParticipants(table, Participant(owner, Role.READY))

        assertEquals(listOf(Participant(owner, Role.READY)), updated.participants)
    }

    @Test
    fun `remove participant returns updated table without that user`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        val withGuest = tableRepo.updateParticipants(table, Participant(guest, Role.SPECTATOR))

        val updated = tableRepo.removeParticipant(withGuest, guest)

        assertEquals(listOf(Participant(owner, Role.PLAYER)), updated.participants)
        assertTrue(tableRepo.getAllParticipants(table.id).none { it.user.id == guest.id })
    }

    @Test
    fun `remove participant ignores users outside the table`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))

        val updated = tableRepo.removeParticipant(table, guest)

        assertEquals(table, updated)
    }

    @Test
    fun `delete by id removes table and its participants`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        tableRepo.updateParticipants(table, Participant(guest, Role.SPECTATOR))

        tableRepo.deleteById(table.id)

        assertNull(tableRepo.findById(table.id))
        assertEquals(emptyList(), tableRepo.getAllParticipants(table.id))
    }

    @Test
    fun `delete by id ignores missing table`() = runBlocking {
        tableRepo.deleteById(999u)

        assertEquals(emptyList(), tableRepo.getAllTables())
    }

    @Test
    fun `get all tables returns all stored tables`(): Unit = runBlocking {
        val first = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        val second = tableRepo.createTable(Name("OtherLobby"), guest, Participant(guest, Role.PLAYER))

        val tables = tableRepo.getAllTables()

        assertEquals(2, tables.size)
        assertNotNull(tables.find { it == first })
        assertNotNull(tables.find { it == second })
    }

    @Test
    fun `clear removes tables and participants`() = runBlocking {
        val table = tableRepo.createTable(Name("Lobby"), owner, Participant(owner, Role.PLAYER))
        tableRepo.updateParticipants(table, Participant(guest, Role.SPECTATOR))

        tableRepo.clear()

        assertEquals(emptyList(), tableRepo.getAllTables())
        assertEquals(emptyList(), tableRepo.getAllParticipants(table.id))
    }
}
