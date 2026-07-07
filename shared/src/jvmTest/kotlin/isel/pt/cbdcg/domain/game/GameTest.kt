package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.MAX_TILES_IN_HAND
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.GameError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameTest {

    @Test
    fun `toGameDTO and toGame preserve game state`() {
        val game = testGame()

        val result = game.toGameDTO().toGame()

        assertEquals(game.id, result.id)
        assertEquals(game.players.map { it.user.id }, result.players.map { it.user.id })
        assertEquals(game.board.tiles.map { it.pos }, result.board.tiles.map { it.pos })
        assertEquals(game.turn, result.turn)
    }

    @Test
    fun `resolveTurnZero moves to substitution when every tile card was placed`() {
        val players = listOf(testPlayer(1u), testPlayer(2u))
        val game = testGame(
            players = players,
            board = testBoardWith(testBoardTile(BoardPosition(0, 0))),
            turn = Turn(0u, listOf(1u, 2u), TurnPhase.CONSTRUCTION, 1_000L),
        )

        val result = game.resolveTurnZero()

        assertEquals(TurnPhase.SUBSTITUTION, result.turn.phase)
        assertEquals(listOf(2u), result.turn.playerTurn)
    }

    @Test
    fun `nextPhase moves construction to substitution when hand has allowed tile count`() {
        val player = testPlayer(1u, hand = mapOf(0u to TileCard(testTile())))
        val game = testGame(players = listOf(player), turn = Turn(1u, listOf(1u), TurnPhase.CONSTRUCTION, 1_000L))

        val result = game.nextPhase()

        assertEquals(TurnPhase.SUBSTITUTION, result.turn.phase)
    }

    @Test
    fun `nextPhase throws in construction when player still has too many tiles`() {
        val hand = (0..MAX_TILES_IN_HAND).associate { it.toUInt() to TileCard(testTile()) }
        val player = testPlayer(1u, hand = hand)
        val game = testGame(players = listOf(player), turn = Turn(1u, listOf(1u), TurnPhase.CONSTRUCTION, 1_000L))

        assertFailsWith<GameError.MustPlaceTile> {
            game.nextPhase()
        }
    }

    @Test
    fun `nextTurn advances to next player`() {
        val players = listOf(
            testPlayer(1u, currentCharacter = "alchemist"),
            testPlayer(2u, currentCharacter = "guardian"),
        )
        val game = testGame(players = players, turn = Turn(1u, listOf(1u, 2u), TurnPhase.MOVEMENT, 1_000L))

        val result = game.nextTurn()

        assertEquals(listOf(2u), result.turn.playerTurn)
    }

    @Test
    fun `startNextTurn draws a tile for the active player`() {
        val deckTile = testTile(listOf(Direction.NORTH, Direction.SOUTH))
        val players = listOf(testPlayer(1u, currentCharacter = "alchemist"))
        val game = testGame(
            players = players,
            tileDeck = mapOf(deckTile to 1u),
            turn = Turn(1u, listOf(1u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.startNextTurn()

        assertTrue(result.players.single().hand.values.any { it == TileCard(deckTile) })
        assertEquals(0u, result.tileDeck.getValue(deckTile))
    }

    @Test
    fun `leaveGame removes player character and returns cards to decks`() {
        val tileCard = TileCard(testTile(listOf(Direction.NORTH)))
        val item = testItem()
        val playerLeaving = testPlayer(
            1u,
            hand = mapOf(0u to tileCard, 1u to ItemCard(item)),
            currentCharacter = "alchemist",
        )
        val stayingPlayer = testPlayer(2u, currentCharacter = "guardian")
        val otherPlayer = testPlayer(3u, currentCharacter = "thief")
        val board = testBoardWith(
            testBoardTile(BoardPosition(0, 0), character = testCharacter("alchemist")),
            testBoardTile(BoardPosition(0, 1), character = testCharacter("guardian")),
            testBoardTile(BoardPosition(1, 0), character = testCharacter("thief")),
        )
        val game = testGame(
            players = listOf(playerLeaving, stayingPlayer, otherPlayer),
            board = board,
            turn = Turn(1u, listOf(2u), TurnPhase.MOVEMENT, 1_000L),
        )

        val result = game.leaveGame(playerLeaving, toSpectator = true)

        assertEquals(listOf(2u, 3u), result.players.map { it.user.id })
        assertTrue(result.spectators.any { it.user.id == 1u })
        assertNull(result.board.tiles.first { it.pos == BoardPosition(0, 0) }.character)
        assertEquals(1u, result.tileDeck.getValue(tileCard.tile))
        assertEquals(1u, result.itemDeck.getValue(item))
    }

    @Test
    fun `placeOnBoard throws when player is not active`() {
        val player = testPlayer(2u, hand = mapOf(0u to TileCard(testTile())))
        val game = testGame(players = listOf(testPlayer(1u), player), turn = Turn(1u, listOf(1u, 2u), TurnPhase.CONSTRUCTION, 1_000L))

        assertFailsWith<GameError.NotYourTurn> {
            game.placeOnBoard(player, BoardPosition(0, 1), player.hand.getValue(0u), 0u)
        }
    }

    @Test
    fun `placeTile puts tile on board and removes card from hand`() {
        val tile = testTile(listOf(Direction.SOUTH))
        val player = testPlayer(1u, hand = mapOf(0u to TileCard(tile)))
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(testBoardTile(BoardPosition(0, 0))),
            turn = Turn(1u, listOf(1u), TurnPhase.CONSTRUCTION, 1_000L),
        )

        val result = game.placeTile(player, BoardPosition(0, 1), TileCard(tile), 0u)

        assertTrue(result.board.tiles.any { it.pos == BoardPosition(0, 1) && it.tile == tile })
        assertTrue(result.players.single().hand.isEmpty())
    }

    @Test
    fun `placeCharacter puts character on board and updates current character`() {
        val character = testCharacter("alchemist")
        val player = testPlayer(1u, hand = mapOf(0u to CharacterCard(character)))
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(testBoardTile(BoardPosition(0, 0))),
            turn = Turn(0u, listOf(1u), TurnPhase.SUBSTITUTION, 1_000L),
        )

        val result = game.placeCharacter(player, BoardPosition(0, 0), CharacterCard(character), 0u)

        assertEquals("alchemist", result.board.tiles.single().character?.name)
        assertEquals("alchemist", result.players.single().currentCharacter)
    }

    @Test
    fun `placeItem equips item and removes it from hand`() {
        val item = testItem()
        val character = testCharacter("alchemist")
        val player = testPlayer(1u, hand = mapOf(0u to ItemCard(item)), currentCharacter = character.name)
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(testBoardTile(BoardPosition(0, 0), character = character)),
            turn = Turn(1u, listOf(1u), TurnPhase.SUBSTITUTION, 1_000L),
        )

        val result = game.placeItem(player, BoardPosition(0, 0), ItemCard(item), 0u)

        val updatedCharacter = result.board.tiles.single().character
        assertTrue(item in (updatedCharacter as isel.pt.cbdcg.domain.game.character.PlayableCharacter).items)
        assertTrue(result.players.single().hand.isEmpty())
    }

    @Test
    fun `unequip removes item from character and returns it to hand`() {
        val item = testItem()
        val character = testCharacter("alchemist").copy(items = listOf(item))
        val player = testPlayer(1u, currentCharacter = character.name)
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(testBoardTile(BoardPosition(0, 0), character = character)),
            turn = Turn(1u, listOf(1u), TurnPhase.SUBSTITUTION, 1_000L),
        )

        val result = game.unequip(player, character, 0)

        val updatedCharacter = result.board.tiles.single().character as isel.pt.cbdcg.domain.game.character.PlayableCharacter
        assertFalse(item in updatedCharacter.items)
        assertTrue(result.players.single().hand.values.any { it == ItemCard(item) })
    }

    @Test
    fun `battle creates pending flee actions for attacker and defender`() {
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

        val result = game.battle(attacker, defender)

        assertNotNull(result.battle)
        assertEquals(BattlePhase.WAITING, result.battle?.phase)
        assertEquals(setOf(attacker.name, defender.name), result.battle?.pending?.map { it.origin.name }?.toSet())
    }

    @Test
    fun `joinBattle adds character to battle pending actions`() {
        val helper = testCharacter("thief")
        val player = testPlayer(3u, currentCharacter = helper.name)
        val battle = Battle(
            characters = listOf(testCharacter("alchemist"), testCharacter("guardian"), helper),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        val game = testGame(players = listOf(player), battle = battle)

        val result = game.joinBattle(player, helper)

        assertTrue(result.battle?.pending?.any { it.origin.name == helper.name } == true)
    }
/*
    @Test
    fun `removeActionFromPending removes queued battle action`() {
        val character = testCharacter("alchemist")
        val action = BattleAction(character, null, PossibleBattleActions.HOLD, Stats(), 1)
        val game = testGame(
            battle = Battle(
                characters = listOf(character, testCharacter("guardian")),
                currentTurn = 1u,
                phase = BattlePhase.BATTLING,
                pending = listOf(action),
                itemBet = emptyList(),
            ),
        )

        val result = game.removeActionFromPending(character)

        assertTrue(result.battle?.pending?.isEmpty() == true)
    }
*/
    @Test
    fun `resolvePending starts battle when every character is ready`() {
        val attacker = testCharacter("alchemist")
        val defender = testCharacter("guardian")
        val battle = Battle(
            characters = listOf(attacker, defender),
            phase = BattlePhase.WAITING,
            pending = listOf(
                BattleAction(attacker, null, PossibleBattleActions.FLEE, Stats(), 1),
                BattleAction(defender, null, PossibleBattleActions.FLEE, Stats(), 1),
            ),
            itemBet = emptyList(),
        )
        val game = testGame(battle = battle)

        val result = game.resolvePending()

        assertEquals(BattlePhase.BATTLING, result.battle?.phase)
        assertEquals(1u, result.battle?.currentTurn)
        assertTrue(result.battle?.pending?.isEmpty() == true)
    }

    @Test
    fun `resolveBattleEnd clears battle after applying rewards and penalties`() {
        val winner = testCharacter("alchemist")
        val loser = testCharacter("guardian", stats = Stats(hp = 1, dmg = 2, def = 1, spe = 2))
        val winnerItem = testItem("winner_item")
        val loserItem = testItem("loser_item")
        val winnerPlayer = testPlayer(
            1u,
            hand = mapOf(0u to ItemCard(winnerItem)),
            currentCharacter = winner.name,
        )
        val loserPlayer = testPlayer(
            2u,
            hand = mapOf(0u to ItemCard(loserItem)),
            currentCharacter = loser.name,
        )
        val battle = Battle(
            characters = listOf(winner, loser),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = listOf(
                BattleBet(winnerPlayer, winnerItem),
                BattleBet(loserPlayer, loserItem),
            ),
        )
        val game = testGame(
            players = listOf(winnerPlayer, loserPlayer),
            board = testBoardWith(
                testBoardTile(BoardPosition(0, 0), character = winner),
                testBoardTile(BoardPosition(0, 1), character = loser),
            ),
            battle = battle,
        )

        val result = game.resolveBattleEnd(battle, winner)

        val updatedWinner = result.players.single { it.user.id == winnerPlayer.user.id }
        assertNull(result.battle)
        assertTrue(updatedWinner.hand.values.any { (it as? ItemCard)?.item == loserItem })
        assertTrue(result.board.tiles.none { it.character?.name == loser.name })
    }

    @Test
    fun `deleteBattle clears battle and keeps surviving character active`() {
        val character = testCharacter("alchemist")
        val player = testPlayer(1u, currentCharacter = character.name)
        val battle = Battle(
            characters = listOf(character),
            phase = BattlePhase.ENDING,
            pending = listOf(BattleAction(character, null, PossibleBattleActions.HOLD, Stats(), 1)),
            itemBet = emptyList(),
        )
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(testBoardTile(BoardPosition(0, 0), character = character)),
            battle = battle,
        )

        val result = game.deleteBattle()

        assertNull(result.battle)
        assertEquals(character.name, result.players.single().currentCharacter)
    }

    @Test
    fun `handleTimeOutOutsideOfBattle advances turn when no forced construction or substitution is needed`() {
        val players = listOf(
            testPlayer(1u, currentCharacter = "alchemist"),
            testPlayer(2u, currentCharacter = "guardian"),
        )
        val game = testGame(players = players, turn = Turn(1u, listOf(1u, 2u), TurnPhase.MOVEMENT, 1_000L))

        val result = game.handleTimeOutOutsideOfBattle()

        assertEquals(listOf(2u), result.turn.playerTurn)
    }

    @Test
    fun `handleTimeOutStartingBattle resolves battle when every character is ready`() {
        val ready = testCharacter("alchemist")
        val alsoReady = testCharacter("guardian")
        val battle = Battle(
            characters = listOf(ready, alsoReady),
            phase = BattlePhase.WAITING,
            pending = listOf(
                BattleAction(ready, null, PossibleBattleActions.FLEE, Stats(), 1),
                BattleAction(alsoReady, null, PossibleBattleActions.FLEE, Stats(), 1),
            ),
            itemBet = emptyList(),
        )
        val game = testGame(battle = battle)

        val result = game.handleTimeOutStartingBattle()

        assertEquals(BattlePhase.BATTLING, result.battle?.phase)
        assertEquals(1u, result.battle?.currentTurn)
    }

    @Test
    fun `placeCharacter throws when replacing a different active character`() {
        val character = testCharacter("guardian")
        val player = testPlayer(1u, hand = mapOf(0u to CharacterCard(character)), currentCharacter = "alchemist")
        val game = testGame(
            players = listOf(player),
            board = testBoardWith(testBoardTile(BoardPosition(0, 0))),
            turn = Turn(1u, listOf(1u), TurnPhase.SUBSTITUTION, 1_000L),
        )

        assertFailsWith<BoardError.CharacterLimitReached> {
            game.placeCharacter(player, BoardPosition(0, 0), CharacterCard(character), 0u)
        }
    }
}
