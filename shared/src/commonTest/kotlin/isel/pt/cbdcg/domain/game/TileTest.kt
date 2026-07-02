package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.canConnectTo
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileTest {

    @Test
    fun `tiles can connect when one has the opposite direction of the other`() {
        val horizontal = Tile(listOf(Direction.EAST))
        val oppositeHorizontal = Tile(listOf(Direction.WEST))

        assertTrue(horizontal.canConnectTo(Direction.EAST, oppositeHorizontal))
    }

    @Test
    fun `tiles do not connect when there is no opposite direction match`() {
        val tile = Tile(listOf(Direction.NORTH))
        val other = Tile(listOf(Direction.NORTH, Direction.EAST))

        assertFalse(tile.canConnectTo(Direction.NORTH, other))
    }
}
