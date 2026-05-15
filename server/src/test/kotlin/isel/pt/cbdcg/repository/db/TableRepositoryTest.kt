package isel.pt.cbdcg.repository.db

import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import org.junit.jupiter.api.BeforeAll
import kotlin.collections.plus
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

    companion object {
        private val userRepo = UserRepositoryDB
        @JvmStatic
        @BeforeAll
        fun init(): Unit {
            dbInit()
            userRepo.save(User(0u, Name("Owner"), Email("owner@gmail.com"), Password("testPassword")))
            userRepo.save(User(0u, Name("Guest"), Email("guest@gmail.com"), Password("testPassword")))
        }
    }

    @BeforeTest
    fun clearRepo() {
        dbInit()
        tableRepo.clear()
        owner = userRepo.findByEmail(Email("owner@gmail.com"))
            ?: userRepo.createUser(Name("Owner"), Email("owner@gmail.com"), Password("testPassword"))
        guest = userRepo.findByEmail(Email("guest@gmail.com"))
            ?: userRepo.createUser(Name("Guest"), Email("guest@gmail.com"), Password("testPassword"))
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
        val participant = Participant(owner, Role.PLAYER)
        val table = tableRepo.createTable(Name("testTable"), owner, participant)

        val found = tableRepo.findByName(Name("testTable"))

        assertEquals(table, found)
    }

    @Test
    fun `find by name returns null when table does not exist`() {
        assertNull(tableRepo.findByName(Name("missing")))
    }

    @Test
    fun `save replaces stored table`() {
        val participant = Participant(owner, Role.PLAYER)
        val table = tableRepo.createTable(Name("testTable"), owner, participant)
        val updated = table.copy(participants = table.participants + Participant(guest, Role.SPECTATOR))

        tableRepo.save(updated)

        assertEquals(updated, tableRepo.findById(table.id))
      //  assertEquals(1, tableRepo.tables.count { it.id == table.id })
    }

    @Test
    fun `remove participant returns updated table without user`() {
        val first = Participant(owner, Role.PLAYER)
        val second = Participant(guest, Role.SPECTATOR)
        val table = tableRepo.createTable(Name("testTable"), owner, first)
        tableRepo.save(table.copy(participants = listOf(first, second)))

        val updated = tableRepo.removeParticipant(tableRepo.findByName(Name("testTable"))!!, guest)

        assertEquals(1, updated.participants.size)
        assertTrue(updated.participants.none { it.user == guest })
    }

    @Test
    fun `update participants replaces participant entry for same user`() {
        val first = Participant(owner, Role.PLAYER)
        val table = tableRepo.createTable(Name("testTable"), owner, first)

        val updated = tableRepo.updateParticipants(table, Participant(owner, Role.SPECTATOR))

        assertEquals(Role.SPECTATOR, updated.participants.single().role)
    }

    @Test
    fun `delete by id removes table`() {
        val table = tableRepo.createTable(Name("testTable"), owner, Participant(owner, Role.PLAYER))

        tableRepo.deleteById(table.id)

        assertNull(tableRepo.findById(table.id))
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
