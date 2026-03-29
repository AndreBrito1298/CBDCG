package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue


class TableServiceTest {

    private val userRepo = UserRepositoryMem
    private val tableRepo = TableRepositoryMem
    private val participantRepo = ParticipantRepositoryMem
    private val tableService = TableService(userRepo, tableRepo, participantRepo)

    //private var alice = userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("alicepassword123!"))
    //private var bea = userRepo.createUser(Name("Bea"), Email("bea@email.com"), Password("beapassword123!"))
    private val aliceEmail = Email("alice@email.com")
    private val beaEmail = Email("bea@email.com")



    @BeforeTest
    fun clearRepo() {
        userRepo.clear()
        tableRepo.clear()
        participantRepo.clear()
        userRepo.createUser(Name("Alice"), Email("alice@email.com"), Password("alicepassword123!"))
        userRepo.createUser(Name("Bea"), Email("bea@email.com"), Password("beapassword123!"))
    }

    @Test
    fun `create table successfully`(){
        val tableName = Name("this test is useless")
        assert(tableService.createTable(tableName, aliceEmail).isSuccess)
        assertTrue(tableRepo.tables.isNotEmpty())
    }

    @Test
    fun `create table same name`(){
        val tableName = Name("this test is not")
        tableService.createTable(tableName, aliceEmail)
        assert(tableService.createTable(tableName, aliceEmail).isFailure)

    }

    @Test
    fun `create table unavailable owner`(){
        val tableName = Name("this test is not")
        val otherName = Name("this wont work")
        tableService.createTable(tableName, aliceEmail)
        tableService.createTable(otherName, aliceEmail)
        assert(tableService.createTable(tableName, aliceEmail).isFailure)
    }

    @Test
    fun `joined table successfully`(){
        val tableName = Name("this test not")

        val table = tableRepo.createTable(tableName, userRepo.findByEmail(aliceEmail)!!.id)
        assert(tableService.joinTable(beaEmail, table.name).isSuccess)
    }

    @Test
    fun `join table all players filled`(){
        val tableName = Name("this test is not")
        val rock = userRepo.createUser(Name("Rock"), Email("rock@email.com"), Password("rockpassword123!"))
        val paper = userRepo.createUser(Name("Paper"), Email("Paper@email.com"), Password("paperpassword123!"))
        val scissors = userRepo.createUser(Name("Scissors"), Email("Scissors@email.com"), Password("scissorspassword123!"))
        val table = tableRepo.createTable(tableName, userRepo.findByEmail(aliceEmail)!!.id)

        tableService.joinTable(rock.email, table.name)
        tableService.joinTable(paper.email, table.name)
        tableService.joinTable(scissors.email, table.name)
        tableService.joinTable(beaEmail, table.name)

        assertEquals(Role.SPECTATOR, participantRepo.participants.last().role)
    }

    @Test
    fun `user cannot join another table`(){

        val user = userRepo.users.first()
        val table = tableRepo.createTable(Name("this test is not"), userRepo.findByEmail(aliceEmail)!!.id)
        participantRepo.joinTable(user, table, Role.PLAYER)

        val otherTable = tableRepo.tables.last()

        assertFailsWith<TableError.UserUnavailable> {
            tableService.joinTable(user.email, otherTable.name).getOrThrow()
        }
    }

    @Test
    fun `user can't leave a table when he is not in it`(){
        val user = userRepo.users.first()
        val table = tableRepo.createTable(Name("this test is not"), userRepo.findByEmail(aliceEmail)!!.id)

        assertFailsWith<TableError.UserNotFound>{
            tableService.leaveTable(user.email, table.name).getOrThrow()
        }
    }




    @Test
    fun `join table unavailable user`(){
        val tableName = Name("this test is not")
        val table = tableRepo.createTable(tableName, userRepo.findByEmail(aliceEmail)!!.id)
        tableService.joinTable(beaEmail, table.name)

        assert(tableService.joinTable(beaEmail, table.name).isFailure)
    }

    @Test
    fun `table does not exist`(){
        assert(tableService.joinTable(beaEmail, Name("who knows")).isFailure)
    }

    @Test
    fun `cannot create table with same name`() {
        val name = Name("testTable")
        tableService.createTable(name, aliceEmail)
        assertFailsWith<TableError.DuplicateName> { tableService.createTable(name, aliceEmail).getOrThrow() }
    }
}