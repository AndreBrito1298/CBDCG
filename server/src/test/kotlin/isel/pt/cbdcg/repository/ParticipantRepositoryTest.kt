package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
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

        participantRepo.clear()
        userRepo.clear()
        tableRepo.clear()

        userRepo.createUser(
            Name("testName"),
            Email("testEmail@gmail.com"),
            Password("testPassword")
        )
        userRepo.createUser(
            Name("randomName"),
            Email("randomEmail@gmail.com"),
            Password("randomPassword")
        )

        tableRepo.createTable(
            Name("tableName"),
            0u
        )

        tableRepo.createTable(
            Name("randomName"),
            1u
        )

    }

    @Test
    fun `user is available to join a table`(){

        val user = userRepo.users.first()
        assertNull(participantRepo.userAvailability(user))
    }

    @Test
    fun `user is not available to join a table`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()
        participantRepo.joinTable(user, table)

        assertNotNull(participantRepo.userAvailability(user))
    }

    @Test
    fun `join a table successfully`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()

        participantRepo.joinTable(user, table)
        assert(participantRepo.participants.size == 1)
    }

    @Test
    fun `user cannot join another table`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()
        participantRepo.joinTable(user, table)

        val otherTable = tableRepo.tables.last()

        assertFailsWith<TableError.UserUnavailable> {
            participantRepo.joinTable(user, otherTable)
        }
    }

    @Test
    fun `user is found in a table`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()
        participantRepo.joinTable(user, table)

        assert(participantRepo.findUserInTable(user, table))

    }

    @Test
    fun `user is not found in a table, when he isn't in the table`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()

        assert(!participantRepo.findUserInTable(user, table))

    }

    @Test
    fun `leave a table successfully`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()
        participantRepo.joinTable(user, table)

        participantRepo.leaveTable(user, table)
        assert(participantRepo.participants.isEmpty())
    }

    @Test
    fun `user can't leave a table when he is not in it`(){

        val user = userRepo.users.first()
        val table = tableRepo.tables.first()

        assertFailsWith<TableError.UserNotFound>{
            participantRepo.leaveTable(user, table)
        }
    }

}