package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.findPath
import isel.pt.cbdcg.domain.game.board.reduceCooldown
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileEffectUsageTest {

    @Test
    fun `applyRandomSpecialEffects preserves deck size and assigns configured effects only to normal tiles`() {
        val normalTile = tile()
        val startTile = tile(effect = TileEffect(TileEffectTypes.Start))
        val deck = mapOf(normalTile to 20u, startTile to 2u)

        val result = deck.applyRandomSpecialEffects()

        assertEquals(22u, result.values.sumOf { it.toInt() }.toUInt())
        assertEquals(2u, result.entries.sumOf { (tile, copies) -> if (tile.specialEffect.type == TileEffectTypes.Start) copies.toInt() else 0 }.toUInt())
        assertEquals(16u, result.entries.sumOf { (tile, copies) -> if (tile.specialEffect.type != TileEffectTypes.None && tile.specialEffect.type != TileEffectTypes.Start) copies.toInt() else 0 }.toUInt())
        assertEquals(4u, result.entries.sumOf { (tile, copies) -> if (tile.specialEffect.type == TileEffectTypes.None) copies.toInt() else 0 }.toUInt())
    }

    @Test
    fun `applyRandomSpecialEffects assigns at most the number of eligible normal tiles`() {
        val normalTile = tile()
        val startTile = tile(effect = TileEffect(TileEffectTypes.Start))
        val deck = mapOf(normalTile to 3u, startTile to 1u)

        val result = deck.applyRandomSpecialEffects()

        assertEquals(4u, result.values.sumOf { it.toInt() }.toUInt())
        assertEquals(3u, result.entries.sumOf { (tile, copies) -> if (tile.specialEffect.type != TileEffectTypes.None && tile.specialEffect.type != TileEffectTypes.Start) copies.toInt() else 0 }.toUInt())
        assertEquals(1u, result.entries.sumOf { (tile, copies) -> if (tile.specialEffect.type == TileEffectTypes.Start) copies.toInt() else 0 }.toUInt())
    }

    @Test
    fun `findPath stops before active special effect tiles`() {
        val start = boardTile(BoardPosition(0, 0), tile(listOf(Direction.EAST)))
        val chest = boardTile(
            BoardPosition(1, 0),
            tile(listOf(Direction.WEST, Direction.EAST), TileEffect(TileEffectTypes.Chest)),
            cooldown = 0u,
        )
        val target = boardTile(BoardPosition(2, 0), tile(listOf(Direction.WEST)))
        val board = Board(listOf(start, chest, target))

        val path = board.findPath(start, target, maxDistance = 2, ignoreCharacters = emptyList())

        assertFalse(path.contains(target))
        assertEquals(chest, path.last())
    }

    @Test
    fun `findPath can pass through special effect tiles while they are in cooldown`() {
        val start = boardTile(BoardPosition(0, 0), tile(listOf(Direction.EAST)))
        val chest = boardTile(
            BoardPosition(1, 0),
            tile(listOf(Direction.WEST, Direction.EAST), TileEffect(TileEffectTypes.Chest)),
            cooldown = 1u,
        )
        val target = boardTile(BoardPosition(2, 0), tile(listOf(Direction.WEST)))
        val board = Board(listOf(start, chest, target))

        val path = board.findPath(start, target, maxDistance = 2, ignoreCharacters = emptyList())

        assertEquals(listOf(start, chest, target), path)
    }

    @Test
    fun `reduceCooldown decreases tile effect cooldown without going below zero`() {
        val active = boardTile(BoardPosition(0, 0), cooldown = 2u)
        val ready = boardTile(BoardPosition(1, 0), cooldown = 0u)
        val board = Board(listOf(active, ready))

        val result = board.reduceCooldown()

        assertEquals(1u, result.tiles.first { it.pos == active.pos }.cooldown)
        assertEquals(0u, result.tiles.first { it.pos == ready.pos }.cooldown)
    }

    @Test
    fun `startNextTurn decreases tile effect modifier duration for active character`() {
        val modifierToKeep = StatModifier(Stats(dmg = 1), duration = 2u, type = ModifierType.TILE_EFFECT)
        val modifierToRemove = StatModifier(Stats(def = 1), duration = 1u, type = ModifierType.TILE_EFFECT)
        val battleModifier = StatModifier(Stats(hp = -1), duration = 0u, type = ModifierType.BATTLE_ATTACK)
        val character = character("alchemist").copy(activeStatModifiers = listOf(modifierToKeep, modifierToRemove, battleModifier))
        val player = player(1u, currentCharacter = character.name)
        val game = Game(
            id = 1u,
            players = listOf(player),
            spectators = emptyList(),
            board = Board(listOf(boardTile(BoardPosition(0, 0), character = character))),
            tileDeck = emptyMap(),
            itemDeck = emptyMap(),
            turn = Turn(1u, listOf(1u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.startNextTurn()
        val updatedCharacter = result.board.tiles.single().character as PlayableCharacter

        assertTrue(updatedCharacter.activeStatModifiers.any { it.type == ModifierType.TILE_EFFECT && it.stats == modifierToKeep.stats && it.duration == 1u })
        assertFalse(updatedCharacter.activeStatModifiers.any { it.type == ModifierType.TILE_EFFECT && it.stats == modifierToRemove.stats })
        assertTrue(updatedCharacter.activeStatModifiers.any { it.type == ModifierType.BATTLE_ATTACK })
    }

    private fun user(id: UInt): User =
        User(id, Name("user$id"), Email("user$id@example.com"), Password("password$id"))

    private fun player(id: UInt, currentCharacter: String?): Player =
        Player(user(id), emptyMap(), currentCharacter)

    private fun character(name: String): PlayableCharacter =
        PlayableCharacter(name, Stats(3, 2, 1, 2), grade = Grade.BASIC, evolution = null)

    private fun tile(
        connections: List<Direction> = Direction.entries,
        effect: TileEffect = TileEffect(),
    ): Tile = Tile(connections, effect)

    private fun boardTile(
        position: BoardPosition,
        tile: Tile = tile(),
        cooldown: UInt = 0u,
        character: isel.pt.cbdcg.domain.game.character.Character? = null,
    ): BoardTile = BoardTile(position, tile, cooldown, character)
}
