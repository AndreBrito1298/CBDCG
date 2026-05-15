package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.User
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TableRepositoryMemTest {
    private val tableRepo = TableRepositoryMem
    private val owner = User(0u, Name("Owner"), Email("owner@gmail.com"), Password("testPassword"))
    private val guest = User(1u, Name("Guest"), Email("guest@gmail.com"), Password("testPassword"))

    @BeforeTest
    fun clearRepo() {
        tableRepo.clear()
    }

    @Test
    fun `create table stores owner and first participant`() {
        val participant = Participant(owner, Role.PLAYER)
        val table = tableRepo.createTable(Name("testTable"), owner, participant)

        assertEquals(table, tableRepo.findById(table.id))
        assertEquals(owner, table.owner)
        assertEquals(listOf(participant), table.participants)
    }

    @Test
    fun `find by name returns stored table`() {
        val table = tableRepo.createTable(Name("testTable"), owner, Participant(owner, Role.PLAYER))
        assertEquals(table, tableRepo.findByName(Name("testTable")))
    }

    @Test
    fun `find by name returns null when table does not exist`() {
        assertNull(tableRepo.findByName(Name("missing")))
    }

    @Test
    fun `update participants replaces participant entry for same user`() {
        val table = tableRepo.createTable(Name("testTable"), owner, Participant(owner, Role.PLAYER))
        val updated = tableRepo.updateParticipants(table, Participant(owner, Role.SPECTATOR))

        assertEquals(Role.SPECTATOR, updated.participants.single().role)
    }

    @Test
    fun `remove participant returns updated table without user`() {
        val first = Participant(owner, Role.PLAYER)
        val second = Participant(guest, Role.SPECTATOR)
        val table = tableRepo.createTable(Name("testTable"), owner, first)
        tableRepo.save(table.copy(participants = listOf(first, second)))

        val updated = tableRepo.removeParticipant(tableRepo.findByName(Name("testTable"))!!, guest)
        assertTrue(updated.participants.none { it.user == guest })
    }

    @Test
    fun `get all tables returns all stored tables`() {
        tableRepo.createTable(Name("testTable"), owner, Participant(owner, Role.PLAYER))
        tableRepo.createTable(Name("otherTable"), guest, Participant(guest, Role.PLAYER))

        val tables = tableRepo.getAllTables()
        assertEquals(2, tables.size)
        assertNotNull(tables.find { it.name.string == "testTable" })
        assertNotNull(tables.find { it.name.string == "otherTable" })
    }
}
