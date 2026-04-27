package isel.pt.cbdcg.domain.game

import kotlin.test.Test
import kotlin.test.assertEquals

class DirectionTest {

    @Test
    fun `opposite direction is correct for all directions`() {
        assertEquals(Direction.SOUTH, Direction.NORTH.opposite())
        assertEquals(Direction.WEST, Direction.EAST.opposite())
        assertEquals(Direction.NORTH, Direction.SOUTH.opposite())
        assertEquals(Direction.EAST, Direction.WEST.opposite())
    }

    @Test
    fun `rotate right cycles through all directions`() {
        assertEquals(Direction.EAST, Direction.NORTH.rotateRight())
        assertEquals(Direction.SOUTH, Direction.EAST.rotateRight())
        assertEquals(Direction.WEST, Direction.SOUTH.rotateRight())
        assertEquals(Direction.NORTH, Direction.WEST.rotateRight())
    }

    @Test
    fun `rotate left cycles through all directions`() {
        assertEquals(Direction.WEST, Direction.NORTH.rotateLeft())
        assertEquals(Direction.NORTH, Direction.EAST.rotateLeft())
        assertEquals(Direction.EAST, Direction.SOUTH.rotateLeft())
        assertEquals(Direction.SOUTH, Direction.WEST.rotateLeft())
    }
}
