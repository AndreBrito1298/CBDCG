package isel.pt.cbdcg.domain

import kotlin.test.Test

class TableTest {

    @Test
    fun `table is available`(){

        val table = Table(1, "testTable".toName(), 1, 3)

        assert(table.checkAvailability())
    }

    @Test
    fun `table is not available`(){

        val table = Table(1, "testTable".toName(), 1, 7)

        assert(!table.checkAvailability())
    }

}