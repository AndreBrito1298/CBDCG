package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ParticipantRepositoryTest {

    private val participantRepo = ParticipantRepositoryMem

    private val user =
        User(1u, Name("testName"), Email("testEmail@gmail.com"), Password("testPassword"))

    @BeforeTest
    fun clearRepo() {
        participantRepo.participants.clear()
    }

    @Test
    fun `create participant stores user and role`() {
        val participant = participantRepo.createParticipant(user, Role.PLAYER)

        assertEquals(1, participantRepo.participants.size)
        assertEquals(Participant(user, Role.PLAYER), participant)
    }

    @Test
    fun `user is available before joining a table`() {
        assertTrue(participantRepo.userAvailability(user))
    }

    @Test
    fun `user is unavailable after becoming participant`() {
        participantRepo.createParticipant(user, Role.SPECTATOR)

        assertFalse(participantRepo.userAvailability(user))
    }

    @Test
    fun `delete participant removes every participant for that user`() {
        participantRepo.createParticipant(user, Role.PLAYER)
        participantRepo.createParticipant(user, Role.SPECTATOR)

        participantRepo.deleteParticipant(user)

        assertTrue(participantRepo.participants.isEmpty())
        assertTrue(participantRepo.userAvailability(user))
    }
}
