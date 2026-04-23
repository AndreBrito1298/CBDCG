package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.BoardPlacementError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BoardTest {

    @Test
    fun `place throws when target position is already taken`() {
        val occupiedPosition = BoardPosition(0, -1)
        val board = Board().place(occupiedPosition, Tile(listOf(Direction.NORTH)))
        assertTrue(board.tiles.containsKey(occupiedPosition))

        assertFailsWith<BoardPlacementError.PositionTaken> {
            board.place(occupiedPosition, Tile(listOf(Direction.NORTH)))
        }
    }

    @Test
    fun `place throws when tile has no adjacent connected tiles`() {
        val board = Board()

        assertFailsWith<BoardPlacementError.TileConnectionMismatch> {
            board.place(BoardPosition(0, 1), Tile(listOf(Direction.NORTH)))
        }

        assertFailsWith<BoardPlacementError.TileConnectionMismatch> {
            board.place(BoardPosition(5, 5), Tile(listOf(Direction.NORTH)))
        }
    }

    @Test
    fun `place returns updated board when tile connects to an adjacent tile`() {
        val initialBoard = Board()

        val firstTile = Tile(listOf(Direction.NORTH, Direction.SOUTH))
        val secondTile = Tile(listOf(Direction.SOUTH, Direction.WEST))
        val thirdTile = Tile(listOf(Direction.NORTH, Direction.EAST))

        val placeFirst = initialBoard.place(BoardPosition(0,1), firstTile)
        val placeSecond = placeFirst.place(BoardPosition(0,2), secondTile)
        val result = placeSecond.place(BoardPosition(-1,0), thirdTile)


        assertEquals(
            mapOf(
                BoardPosition(0,0) to Tile(Direction.entries),
                BoardPosition(0,1) to firstTile,
                BoardPosition(0,2) to secondTile,
                BoardPosition(-1,0) to thirdTile
            ),
            result.tiles
        )
    }
}
