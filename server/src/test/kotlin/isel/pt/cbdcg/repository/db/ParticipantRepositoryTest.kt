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
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParticipantRepositoryTest {

    private val participantRepo = ParticipantRepositoryDB
    private val userRepo = UserRepositoryDB
    private val tableRepo = TableRepositoryDB

    private val seedUser =
        User(1u, Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

    private val seedOwner =
        User(2u, Name("ownerNamee"), Email("ownerEmail@gmail.com"), Password("testPassword"))

    private lateinit var user: User
    private lateinit var owner: User
    private lateinit var table: Table

    @BeforeTest
    fun clearRepo() {
        dbInit()
        participantRepo.clear()
        tableRepo.clear()
        userRepo.clear()
        userRepo.save(seedUser)
        userRepo.save(seedOwner)
        user = userRepo.findByEmail(seedUser.email)!!
        owner = userRepo.findByEmail(seedOwner.email)!!
        table = Table(1u, Name("TestName"), owner, emptyList())
        tableRepo.save(table)
        table = tableRepo.findByName(table.name)!!
    }

    @Test
    fun `create participant stores user and role`() {
        val participant = participantRepo.createParticipant(user, table, Role.PLAYER)

        assertEquals(Participant(user, Role.PLAYER), participant)
    }

    @Test
    fun `user is available before joining a table`() {
        assertTrue(participantRepo.userAvailability(user))
    }

    @Test
    fun `user is unavailable after becoming participant`() {
        participantRepo.createParticipant(user, table, Role.SPECTATOR)

        assertFalse(participantRepo.userAvailability(user))
    }

    @Test
    fun `delete participant removes every participant for that user`() {
        participantRepo.createParticipant(user, table, Role.PLAYER)
        participantRepo.createParticipant(user, table, Role.SPECTATOR)

        participantRepo.deleteParticipant(user)

        assertTrue(participantRepo.userAvailability(user))
    }
}
