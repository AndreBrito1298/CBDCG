package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.connectedNeighbours
import isel.pt.cbdcg.domain.game.board.findPath
import isel.pt.cbdcg.domain.game.board.placeTile
import isel.pt.cbdcg.domain.game.board.possibleUnoccupiedPositions
import isel.pt.cbdcg.domain.game.board.reduceCooldown
import isel.pt.cbdcg.domain.game.board.replaceBoardTile
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.error.BoardError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class BoardTest {

    @Test
    fun `placeTile throws when target position is already taken`() {
        val occupiedPosition = BoardPosition(0, 0)
        val board = Board()

        assertFailsWith<BoardError.PositionTaken> {
            board.placeTile(occupiedPosition, Tile(listOf(Direction.NORTH)))
        }
    }

    @Test
    fun `placeTile throws when tile cannot connect to adjacent tiles`() {
        val board = Board()

        assertFailsWith<BoardError.TileConnectionMismatch> {
            board.placeTile(BoardPosition(0, 1), Tile(listOf(Direction.EAST)))
        }
    }

    @Test
    fun `placeTile returns updated board when tile connects to start`() {
        val tile = Tile(listOf(Direction.SOUTH, Direction.EAST))

        val result = Board().placeTile(BoardPosition(0, 1), tile)

        assertTrue(result.tiles.any { it.pos == BoardPosition(0, 1) && it.tile == tile })
        assertEquals(2, result.tiles.size)
    }

    @Test
    fun `connectedNeighbours returns only mutually connected adjacent tiles`() {
        val origin = BoardTile(BoardPosition(0, 0), Tile(listOf(Direction.NORTH, Direction.EAST)), 0u, null)
        val connected = BoardTile(BoardPosition(0, 1), Tile(listOf(Direction.SOUTH)), 0u, null)
        val blocked = BoardTile(BoardPosition(1, 0), Tile(listOf(Direction.NORTH)), 0u, null)
        val board = Board(listOf(origin, connected, blocked))

        assertEquals(listOf(connected), board.connectedNeighbours(origin))
    }

    @Test
    fun `findPath returns path through connected tiles within distance`() {
        val start = BoardTile(BoardPosition(0, 0), Tile(listOf(Direction.NORTH)), 0u, null)
        val middle = BoardTile(BoardPosition(0, 1), Tile(listOf(Direction.SOUTH, Direction.NORTH)), 0u, null)
        val end = BoardTile(BoardPosition(0, 2), Tile(listOf(Direction.SOUTH)), 0u, null)
        val board = Board(listOf(start, middle, end))

        assertEquals(listOf(start, middle, end), board.findPath(start, end, maxDistance = 2, ignoreCharacters = emptyList()))
    }

    @Test
    fun `reduceCooldown never goes below zero`() {
        val cooling = BoardTile(BoardPosition(0, 0), Tile(Direction.entries), 2u, null)
        val ready = BoardTile(BoardPosition(0, 1), Tile(Direction.entries), 0u, null)

        val result = Board(listOf(cooling, ready)).reduceCooldown()

        assertEquals(1u, result.tiles.first { it.pos == cooling.pos }.cooldown)
        assertEquals(0u, result.tiles.first { it.pos == ready.pos }.cooldown)
    }

    @Test
    fun `replaceBoardTile swaps tile by position`() {
        val old = BoardTile(BoardPosition(0, 0), Tile(listOf(Direction.NORTH)), 0u, null)
        val updated = old.copy(tile = Tile(listOf(Direction.SOUTH)))

        val result = Board(listOf(old)).replaceBoardTile(updated)

        assertEquals(listOf(updated), result.tiles)
    }

    @Test
    fun `possibleUnoccupiedPositions returns unique adjacent empty positions`() {
        val positions = Board().possibleUnoccupiedPositions()

        assertEquals(4, positions.size)
        assertTrue(BoardPosition(0, 1) in positions)
        assertTrue(BoardPosition(1, 0) in positions)
    }
}
