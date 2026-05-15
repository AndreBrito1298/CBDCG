package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParticipantRepositoryMemTest {

    private val participantRepo = ParticipantRepositoryMem

    private val user =
        User(1u, Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

    private val owner =
        User(2u, Name("ownerNamee"), Email("ownerEmail@gmail.com"), Password("testPassword"))

    private val table = Table(1u, Name("TestName"), owner, emptyList())

    @BeforeTest
    fun clearRepo() {
        participantRepo.clear()
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
