package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.Tile
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
/*
class BoardTest {

    @Test
    fun `place throws when target position is already taken`() {
        val occupiedPosition = BoardPosition(0, -1)
        val board = Board().place(occupiedPosition, Tile(listOf(Direction.NORTH)))
        assertTrue(board.tiles.any{ it.pos == occupiedPosition})

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
        val firstPos = BoardPosition(0, 1)
        val secondTile = Tile(listOf(Direction.SOUTH, Direction.WEST))
        val secondPos = BoardPosition(0, 2)
        val thirdTile = Tile(listOf(Direction.NORTH, Direction.EAST))
        val thirdPos = BoardPosition(-1, 0)

        val placeFirst = initialBoard.place(firstPos, firstTile)
        val placeSecond = placeFirst.place(secondPos, secondTile)
        val result = placeSecond.place(thirdPos, thirdTile)


        assertTrue(result.tiles.containsAll(listOf(
            BoardTile(firstPos, firstTile),
            BoardTile(secondPos, secondTile),
            BoardTile(thirdPos, thirdTile)
        )))
    }
}
*/