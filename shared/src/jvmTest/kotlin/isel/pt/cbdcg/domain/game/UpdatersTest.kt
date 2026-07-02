package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.StatType
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.error.BattleError
import isel.pt.cbdcg.error.BoardError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class UpdatersTest {

    @Test
    fun `CharacterMovement updater moves character between board tiles`() {
        val character = testCharacter("alchemist")
        val origin = testBoardTile(BoardPosition(0, 0), character = character)
        val target = testBoardTile(BoardPosition(1, 0))
        val game = testGame(
            board = testBoardWith(origin, target),
            turn = Turn(1u, listOf(1u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.gameUpdateByName("CharacterMovement", origin, listOf(target))

        assertNull(result.board.tiles.first { it.pos == origin.pos }.character)
        assertEquals(character.name, result.board.tiles.first { it.pos == target.pos }.character?.name)
    }

    @Test
    fun `CharacterMovement updater throws when target is occupied`() {
        val origin = testBoardTile(BoardPosition(0, 0), character = testCharacter("alchemist"))
        val target = testBoardTile(BoardPosition(1, 0), character = testCharacter("guardian"))
        val game = testGame(
            board = testBoardWith(origin, target),
            turn = Turn(1u, listOf(1u), TurnPhase.MOVEMENT, 1_000L),
        )

        assertFailsWith<BoardError.TileOccupied> {
            game.gameUpdateByName("CharacterMovement", origin, listOf(target))
        }
    }

    @Test
    fun `DrawItem updater draws one item and starts tile cooldown`() {
        val item = testItem()
        val player = testPlayer(1u, currentCharacter = "alchemist")
        val chest = testBoardTile(BoardPosition(0, 0), tile = chestTile(cooldown = 2u))
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(chest),
            itemDeck = mapOf(item to 1u),
            turn = Turn(1u, listOf(1u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.gameUpdateByName("DrawItem", player, listOf(chest))

        assertEquals(2u, result.board.tiles.single().cooldown)
        assertTrue(result.players.single().hand.values.any { it == ItemCard(item) })
        assertEquals(0u, result.itemDeck.getValue(item))
    }

    @Test
    fun `UpdateStatModifiers updater applies effect to connected characters`() {
        val originCharacter = testCharacter("alchemist")
        val affectedCharacter = testCharacter("guardian")
        val effect = TileEffect(
            type = TileEffectTypes.StatUpAoE(StatType.Dmg),
            range = 1u,
            maxCooldown = 2u,
        )
        val origin = testBoardTile(
            BoardPosition(0, 0),
            tile = testTile(listOf(Direction.NORTH), effect),
            character = originCharacter,
        )
        val affected = testBoardTile(
            BoardPosition(0, 1),
            tile = testTile(listOf(Direction.SOUTH)),
            character = affectedCharacter,
        )
        val player = testPlayer(1u, currentCharacter = originCharacter.name)
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(origin, affected),
            turn = Turn(1u, listOf(1u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.gameUpdateByName("UpdateStatModifiers", player, listOf(origin))

        assertEquals(2u, result.board.tiles.first { it.pos == origin.pos }.cooldown)
        assertTrue(
            result.board.tiles
                .mapNotNull { it.character }
                .all { character -> character.activeStatModifiers.any { it.type == ModifierType.TILE_EFFECT } }
        )
    }

    @Test
    fun `BattleStart updater starts battle through polymorphic dispatch`() {
        val attacker = testCharacter("alchemist")
        val defender = testCharacter("guardian")
        val game = testGame(
            players = listOf(
                testPlayer(1u, currentCharacter = attacker.name),
                testPlayer(2u, currentCharacter = defender.name),
            ),
            board = testBoardWith(
                testBoardTile(BoardPosition(0, 0), character = attacker),
                testBoardTile(BoardPosition(0, 1), character = defender),
            ),
            turn = Turn(1u, listOf(1u, 2u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.gameUpdateByName("BattleStart", attacker, listOf(defender))

        assertNotNull(result.battle)
        assertEquals(BattlePhase.BATTLING, result.battle?.phase)
        assertEquals(1u, result.battle?.currentTurn)
    }

    @Test
    fun `JoinBattle updater queues joining character`() {
        val helper = testCharacter("thief")
        val player = testPlayer(3u, currentCharacter = helper.name)
        val battle = Battle(
            characters = listOf(testCharacter("alchemist"), testCharacter("guardian"), helper),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        val game = testGame(players = listOf(player), battle = battle)

        val result = game.gameUpdateByName("JoinBattle", player, listOf(helper))

        assertTrue(result.battle?.pending?.any { it.origin.name == helper.name } == true)
    }

    @Test
    fun `AddActionToPending updater queues action`() {
        val character = testCharacter("alchemist")
        val action = BattleAction(character, null, PossibleBattleActions.HOLD, Stats(), 1)
        val battle = Battle(
            characters = listOf(character, testCharacter("guardian")),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        val game = testGame(battle = battle)

        val result = game.gameUpdateByName("AddActionToPending", action, emptyList())

        assertEquals(listOf(action), result.battle?.pending)
    }

    @Test
    fun `AddActionToPending updater rejects duplicate character action`() {
        val character = testCharacter("alchemist")
        val action = BattleAction(character, null, PossibleBattleActions.HOLD, Stats(), 1)
        val battle = Battle(
            characters = listOf(character, testCharacter("guardian")),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            pending = listOf(action),
            itemBet = emptyList(),
        )
        val game = testGame(battle = battle)

        assertFailsWith<BattleError.ActionAlreadyQueued> {
            game.gameUpdateByName("AddActionToPending", action, emptyList())
        }
    }

    @Test
    fun `LeaveBattle updater removes non-main character and deletes concluded battle`() {
        val attacker = testCharacter("alchemist")
        val defender = testCharacter("guardian")
        val support = testCharacter("thief")
        val battle = Battle(
            characters = listOf(attacker, defender, support),
            phase = BattlePhase.ENDING,
            pending = listOf(
                BattleAction(attacker, null, PossibleBattleActions.HOLD, Stats(), 1),
                BattleAction(defender, null, PossibleBattleActions.HOLD, Stats(), 1),
            ),
            itemBet = emptyList(),
        )
        val players = listOf(
            testPlayer(1u, currentCharacter = attacker.name),
            testPlayer(2u, currentCharacter = defender.name),
            testPlayer(3u, currentCharacter = support.name),
        )
        val game = testGame(
            players = players,
            board = testBoardWith(
                testBoardTile(BoardPosition(0, 0), character = attacker),
                testBoardTile(BoardPosition(0, 1), character = defender),
                testBoardTile(BoardPosition(1, 0), character = support),
            ),
            battle = battle,
        )

        val result = game.gameUpdateByName("LeaveBattle", support, emptyList())

        assertNull(result.battle)
    }

    @Test
    fun `RemoveActionFromPending updater removes queued action`() {
        val character = testCharacter("alchemist")
        val action = BattleAction(character, null, PossibleBattleActions.HOLD, Stats(), 1)
        val battle = Battle(
            characters = listOf(character, testCharacter("guardian")),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            pending = listOf(action),
            itemBet = emptyList(),
        )
        val game = testGame(battle = battle)

        val result = game.gameUpdateByName("RemoveActionFromPending", character, emptyList())

        assertTrue(result.battle?.pending?.isEmpty() == true)
    }

    @Test
    fun `unknown updater name throws`() {
        val game = testGame()

        assertFailsWith<IllegalArgumentException> {
            game.gameUpdateByName("MissingUpdater", testCharacter("alchemist"), emptyList())
        }
    }
}

