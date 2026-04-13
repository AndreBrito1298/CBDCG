package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.*
import isel.pt.cbdcg.error.*
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.service.events.TableEventsPublisher
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class TableServiceTest {

    private val userRepo = UserRepositoryMem
    private val tableRepo = TableRepositoryMem
    private val participantRepo = ParticipantRepositoryMem
    private val dummyEvents = object : TableEventsPublisher {
        override suspend fun publishLobbyTables(tables: List<Table>) {}
        override suspend fun publishTableUpdated(table: Table) {}
    }
    private val tableService = TableService(userRepo, tableRepo, participantRepo, dummyEvents)

    private val aliceEmail = Email("alice@email.com")
    private val beaEmail = Email("bea@email.com")

    private fun getToken(email: Email): String {
        val user = userRepo.findByEmail(email)!!
        if (user.auth == null) {
            val token = "dummy-token-${user.id}"
            userRepo.save(user.copy(auth = AuthUser(token, user.email, user.name)))
            return token
        }
        return user.auth!!.token
    }

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
        tableRepo.clear()
        participantRepo.clear()
        userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("alicepassword123!"))
        userRepo.createUser(Name("Bea"), Email("bea@email.com"), Password("beapassword123!"))
    }

    @Test
    fun `create table successfully`() = runBlocking {
        val tableName = Name("this test is useless")
        assert(tableService.createTableWithEmail(tableName, aliceEmail, getToken(aliceEmail)).isSuccess)
        assertTrue(tableRepo.tables.isNotEmpty())
    }

    @Test
    fun `create table same name`() = runBlocking {
        val tableName = Name("this test is not")
        tableService.createTableWithEmail(tableName, aliceEmail, getToken(aliceEmail))
        assert(tableService.createTableWithEmail(tableName, aliceEmail, getToken(aliceEmail)).isFailure)
    }

    @Test
    fun `create table unavailable owner`() = runBlocking {
        val tableName = Name("this test is not")
        val otherName = Name("this wont work")
        tableService.createTableWithEmail(tableName, aliceEmail, getToken(aliceEmail))
        assert(tableService.createTableWithEmail(otherName, aliceEmail, getToken(aliceEmail)).isFailure)
    }

    @Test
    fun `joined table successfully`() = runBlocking {
        val tableName = Name("this test not")
        val owner = userRepo.findByEmail(aliceEmail)!!
        val participant = participantRepo.createParticipant(owner, Role.PLAYER)
        val table = tableRepo.createTable(tableName, owner, participant)
        assert(tableService.joinTableWithEmailAndName(beaEmail, table.name, getToken(beaEmail)).isSuccess)
    }

    @Test
    fun `join table all players filled`() = runBlocking {
        val tableName = Name("this test is not")
        val rock = userRepo.createUser(Name("Rock"), Email("rock@email.com"), Password("rockpassword123!"))
        val paper = userRepo.createUser(Name("Paper"), Email("Paper@email.com"), Password("paperpassword123!"))
        val scissors = userRepo.createUser(Name("Scissors"), Email("Scissors@email.com"), Password("scissorspassword123!"))
        val owner = userRepo.findByEmail(aliceEmail)!!
        val participant = participantRepo.createParticipant(owner, Role.PLAYER)
        val table = tableRepo.createTable(tableName, owner, participant)

        tableService.joinTableWithEmailAndName(rock.email, table.name, getToken(rock.email))
        tableService.joinTableWithEmailAndName(paper.email, table.name, getToken(paper.email))
        tableService.joinTableWithEmailAndName(scissors.email, table.name, getToken(scissors.email))
        tableService.joinTableWithEmailAndName(beaEmail, table.name, getToken(beaEmail))

        assertEquals(Role.SPECTATOR, participantRepo.participants.last().role)
    }

    @Test
    fun `user cannot join another table`() {
        runBlocking {
            val user = userRepo.users.first()
            val owner = userRepo.findByEmail(aliceEmail)!!
            val participant = participantRepo.createParticipant(owner, Role.PLAYER)
            val table = tableRepo.createTable(Name("this test is not"), owner, participant)

            val otherTable = tableRepo.tables.last()

            assertFailsWith<TableError.UserUnavailable> {
                tableService.joinTableWithEmailAndName(user.email, otherTable.name, getToken(user.email)).getOrThrow()
            }
        }
    }

    @Test
    fun `user can't leave a table when he is not in it`() {
        runBlocking {
            val user = userRepo.users.first()
            val owner = userRepo.findByEmail(aliceEmail)!!
            val participant = participantRepo.createParticipant(owner, Role.PLAYER)
            val table = tableRepo.createTable(Name("this test is not"), owner, participant)

            assertFailsWith<TableError.UserNotFound>{
                tableService.leaveTableWithEmailAndName(user.email, table.name, getToken(user.email)).getOrThrow()
            }
        }
    }

    @Test
    fun `join table unavailable user`() = runBlocking {
        val tableName = Name("this test is not")
        val owner = userRepo.findByEmail(aliceEmail)!!
        val participant = participantRepo.createParticipant(owner, Role.PLAYER)
        val table = tableRepo.createTable(tableName, owner, participant)
        tableService.joinTableWithEmailAndName(beaEmail, table.name, getToken(beaEmail))

        assert(tableService.joinTableWithEmailAndName(beaEmail, table.name, getToken(beaEmail)).isFailure)
    }

    @Test
    fun `table does not exist`() = runBlocking {
        assert(tableService.joinTableWithEmailAndName(beaEmail, Name("who knows"), getToken(beaEmail)).isFailure)
    }

    @Test
    fun `cannot create table with same name`() {
        runBlocking {
            val name = Name("testTable")
            tableService.createTableWithEmail(name, aliceEmail, getToken(aliceEmail))
            assertFailsWith<TableError.DuplicateName> { tableService.createTableWithEmail(name, aliceEmail, getToken(aliceEmail)).getOrThrow() }
        }
    }
}