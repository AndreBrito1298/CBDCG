package isel.pt.cbdcg.repository


import isel.pt.cbdcg.domain.*
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

        userRepo.save(
            User(
                1u,
                Name("testName"),
                Email("testEmail@gmail.com"),
                Password("testPassword")
            )
        )
        userRepo.save(
            User(
                2u,
                Name("randomName"),
                Email("randomEmail@gmail.com"),
                Password("randomPassword")
            )
        )

        tableRepo.clear()
    }

    @Test
    fun `create table successfully`() {

        val name = Name("testTable")
        val owner = userRepo.findById(1u)!!
        val participant = Participant(owner, Role.PLAYER)

        tableRepo.createTable(name, owner, participant)
        assert(tableRepo.tables.find{ it.name.string == name.string } != null)
    }

}