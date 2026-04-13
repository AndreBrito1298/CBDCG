package isel.pt.cbdcg.repository

import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.domain.*
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ParticipantRepositoryTest {

    private val userRepo = UserRepositoryMem

    private val tableRepo = TableRepositoryMem

    private val participantRepo = ParticipantRepositoryMem

    @BeforeTest
    fun clearRepo(){

        participantRepo.participants.clear()
        userRepo.clear()
        tableRepo.clear()

        val user1 = userRepo.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword")
        )
        val user2 = userRepo.createUser(
            Name("randomName"),
            Email("randomEmail@gmail.com"),
            Password("randomPassword")
        )

        val participant1 = Participant(user1, Role.PLAYER)
        tableRepo.createTable(
            Name("tableName"),
            user1,
            participant1
        )

        val participant2 = Participant(user2, Role.PLAYER)
        tableRepo.createTable(
            Name("randomName"),
            user2,
            participant2
        )

    }

    @Test
    fun `user is available to join a table`(){

        val user = userRepo.users.first()
        assert(participantRepo.userAvailability(user))
    }

    @Test
    fun `user is not available to join a table`(){

        val user = userRepo.users.first()
        participantRepo.createParticipant(user, Role.PLAYER)

        assert(!participantRepo.userAvailability(user))
    }

    @Test
    fun `join a table successfully`(){

        val user = userRepo.users.first()

        participantRepo.createParticipant(user, Role.PLAYER)
        assert(participantRepo.participants.size == 1)
    }

    @Test
    fun `user is found as a participant`(){

        val user = userRepo.users.first()
        participantRepo.createParticipant(user, Role.PLAYER)

        assert(!participantRepo.userAvailability(user))

    }

    @Test
    fun `user is not found as a participant, when he isn't participating`(){

        val user = userRepo.users.first()

        assert(participantRepo.userAvailability(user))

    }

    @Test
    fun `leave a table successfully`(){

        val user = userRepo.users.first()
        participantRepo.createParticipant(user, Role.PLAYER)

        participantRepo.deleteParticipant(user)
        assert(participantRepo.participants.isEmpty())
    }



}