package isel.pt.cbdcg.domain

import kotlin.test.Test

class TableTest {

    @Test
    fun `table is available`(){
        val user = User(1u, Name("test"), Email("test@test.com"), Password("password"))
        val table = Table(1u, Name("testTable"), user, emptyList())

        assert(table.checkAvailability())
    }

    @Test
    fun `table is not available`(){
        val user = User(1u, Name("test"), Email("test@test.com"), Password("password"))
        val participants = listOf(
            Participant(user, Role.PLAYER),
            Participant(user, Role.PLAYER),
            Participant(user, Role.PLAYER),
            Participant(user, Role.PLAYER)
        )
        val table = Table(1u, Name("testTable"), user, participants)

        assert(!table.checkAvailability())
    }

}