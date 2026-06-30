package isel.pt.cbdcg.repository.db

import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.database.ParticipantRepositoryDB
import isel.pt.cbdcg.repository.database.TableRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParticipantRepositoryTest {

    private val participantRepo = ParticipantRepositoryDB
    private val tableRepo = TableRepositoryDB
    private val userRepo = UserRepositoryDB

    private lateinit var owner: User
    private lateinit var guest: User
    private lateinit var table: Table

    @BeforeTest
    fun resetDb() = runBlocking {
        dbInit(reset = true)
        owner = userRepo.createUser(Name("Owner"), Email("owner@email.com"), Password("secret1"))
        guest = userRepo.createUser(Name("Guest"), Email("guest@email.com"), Password("secret1"))
        table = tableRepo.saveAndFind(Table(10u, Name("Lobby"), owner, emptyList()))
    }

    private suspend fun TableRepositoryDB.saveAndFind(table: Table): Table {
        save(table)
        return findById(table.id)!!
    }

    @Test
    fun `create participant stores user table and role`() = runBlocking {
        val participant = participantRepo.createParticipant(guest, table, Role.PLAYER)

        assertEquals(Participant(guest, Role.PLAYER), participant)
        assertEquals(participant, participantRepo.findById(guest.id))
        assertEquals(listOf(participant), tableRepo.getAllParticipants(table.id))
    }

    @Test
    fun `user is available before joining a table`() = runBlocking {
        assertTrue(participantRepo.userAvailability(guest))
    }

    @Test
    fun `user is unavailable after becoming participant`() = runBlocking {
        participantRepo.createParticipant(guest, table, Role.SPECTATOR)

        assertFalse(participantRepo.userAvailability(guest))
    }

    @Test
    fun `create participant updates existing row for same user`() = runBlocking {
        participantRepo.createParticipant(guest, table, Role.SPECTATOR)

        val updated = participantRepo.createParticipant(guest, table, Role.READY)

        assertEquals(Participant(guest, Role.READY), updated)
        assertEquals(listOf(updated), tableRepo.getAllParticipants(table.id))
    }

    @Test
    fun `save updates participant role`() = runBlocking {
        participantRepo.createParticipant(guest, table, Role.SPECTATOR)

        participantRepo.save(Participant(guest, Role.READY))

        assertEquals(Participant(guest, Role.READY), participantRepo.findById(guest.id))
    }

    @Test
    fun `save does not create participant without an existing table row`() = runBlocking {
        participantRepo.save(Participant(guest, Role.READY))

        assertNull(participantRepo.findById(guest.id))
        assertTrue(participantRepo.userAvailability(guest))
    }

    @Test
    fun `delete participant removes participant for that user`() = runBlocking {
        participantRepo.createParticipant(guest, table, Role.PLAYER)

        participantRepo.deleteParticipant(guest)

        assertNull(participantRepo.findById(guest.id))
        assertTrue(participantRepo.userAvailability(guest))
        assertEquals(emptyList(), tableRepo.getAllParticipants(table.id))
    }

    @Test
    fun `delete by id removes participant for that user id`() = runBlocking {
        participantRepo.createParticipant(guest, table, Role.PLAYER)

        participantRepo.deleteById(guest.id)

        assertNull(participantRepo.findById(guest.id))
        assertTrue(participantRepo.userAvailability(guest))
    }

    @Test
    fun `delete by id ignores missing participant`() = runBlocking {
        participantRepo.deleteById(999u)

        assertTrue(participantRepo.userAvailability(guest))
    }

    @Test
    fun `clear removes every participant`() = runBlocking {
        participantRepo.createParticipant(owner, table, Role.PLAYER)
        participantRepo.createParticipant(guest, table, Role.SPECTATOR)

        participantRepo.clear()

        assertTrue(participantRepo.userAvailability(owner))
        assertTrue(participantRepo.userAvailability(guest))
        assertEquals(emptyList(), tableRepo.getAllParticipants(table.id))
    }
}
