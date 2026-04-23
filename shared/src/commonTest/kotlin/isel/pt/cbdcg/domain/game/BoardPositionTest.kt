package isel.pt.cbdcg.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals

class BoardPositionTest {

    @Test
    fun `neighbour returns adjacent position for each direction`() {
        val position = BoardPosition(3, 4)

        assertEquals(BoardPosition(3, 5), position.neighbour(Direction.NORTH))
        assertEquals(BoardPosition(4, 4), position.neighbour(Direction.EAST))
        assertEquals(BoardPosition(3, 3), position.neighbour(Direction.SOUTH))
        assertEquals(BoardPosition(2, 4), position.neighbour(Direction.WEST))
    }
}
