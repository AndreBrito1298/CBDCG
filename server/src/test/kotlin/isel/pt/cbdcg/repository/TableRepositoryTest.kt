package isel.pt.cbdcg.repository


import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith


class TableRepositoryTest {

    private val userRepo = UserRepositoryMem

    private val tableRepo = TableRepositoryMem

    @BeforeTest
    fun clearRepo() {
        userRepo.clear()

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

        tableRepo.clear()
    }

    @Test
    fun `create table successfully`() {

        val name = Name("testTable")

        tableRepo.createTable(name, 0u)
        assert(tableRepo.tables.find{ it.name.string == name.string } != null)

    }

    @Test
    fun `cannot create table with same name`() {

        val name = Name("testTable")
        tableRepo.createTable(name, 0u)

        assertFailsWith<TableError.DuplicateName> { tableRepo.createTable(name, 0u) }

    }

}