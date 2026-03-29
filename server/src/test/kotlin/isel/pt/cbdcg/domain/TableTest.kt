package isel.pt.cbdcg.domain

import kotlin.test.Test

class TableTest {

    @Test
    fun `table is available`(){

        val table = Table(1u, "testTable".toName(), 1u, 3u)

        assert(table.checkAvailability())
    }

    @Test
    fun `table is not available`(){

        val table = Table(1u, "testTable".toName(), 1u, 7u)

        assert(!table.checkAvailability())
    }

}