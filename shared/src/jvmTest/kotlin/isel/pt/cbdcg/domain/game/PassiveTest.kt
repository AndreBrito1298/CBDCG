package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.PaladinBasic
import isel.pt.cbdcg.domain.game.character.Passive
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.PriestRare
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.usePassive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PassiveTest {

    @Test
    fun `Passive usePassive applies modifier through playable character receiver`() {
        val tracedModifier = StatModifier(Stats(dmg = 2), 3u, ModifierType.TMP_PASSIVE_MODIFIER)
        val tracedPassive = Passive {
            addModifier(tracedModifier) as PlayableCharacter
        }
        val character = passiveCharacter("traced_passive", tracedPassive)
        val battle = waitingBattle(character, passiveCharacter("target", Passive { this }))

        val result = tracedPassive.usePassive(character, battle)

        assertPassiveApplications(
            moment = "direct passive structure",
            character = result,
            expected = listOf(tracedModifier),
        )
    }

    @Test
    fun `resolvePending applies canUsePassive passive once at battle start`() {
        val priestModifier = StatModifier(Stats(hp = 1, dmg = 1, def = 1, spe = 1), 1u, ModifierType.TMP_PASSIVE_MODIFIER)
        val priest = passiveCharacter("priestess", PriestRare, stats = Stats(hp = 4, dmg = 2, def = 3, spe = 2))
        val target = passiveCharacter("target", Passive { this }, stats = Stats(hp = 9, dmg = 1, def = 1, spe = 1))
        val game = gameWithBattle(waitingBattle(priest, target))

        val afterBattleStart = game.resolvePending()
        val priestAtStart = afterBattleStart.battle!!.character("priestess")

        assertFalse((priestAtStart as PlayableCharacter).canUsePassive, "battle start should mark priestess passive as spent")
        assertPassiveApplications(
            moment = "battle start",
            character = priestAtStart,
            expected = listOf(priestModifier),
        )

        val afterRoundOne = afterBattleStart
            .withPending(
                BattleAction(priestAtStart, null, PossibleBattleActions.HOLD, Stats(), 1),
                BattleAction(afterBattleStart.battle!!.character("target"), null, PossibleBattleActions.HOLD, Stats(), 1),
            )
            .resolvePending()
        val priestAfterRoundOne = afterRoundOne.battle!!.character("priestess")

        assertPassiveApplications(
            moment = "round 1 should not spend canUsePassive again",
            character = priestAfterRoundOne,
            expected = listOf(priestModifier.copy(duration = 2u)),
        )
    }

    @Test
    fun `resolvePending applies passives that do not spend canUsePassive every round while condition applies`() {
        val shield = Item("round_shield", Stats(def = 2), Grade.BASIC)
        val paladinModifier = StatModifier(Stats(dmg = 2, def = -2), 0u, ModifierType.TMP_PASSIVE_MODIFIER)
        val paladin = passiveCharacter(
            name = "guardian",
            passive = PaladinBasic,
            stats = Stats(hp = 8, dmg = 2, def = 5, spe = 2),
            items = listOf(shield),
        )
        val target = passiveCharacter("target", Passive { this }, stats = Stats(hp = 9, dmg = 1, def = 1, spe = 1))
        val game = gameWithBattle(waitingBattle(paladin, target))

        val afterBattleStart = game.resolvePending()
        val paladinAtStart = afterBattleStart.battle!!.character("guardian")

        assertTrue((paladinAtStart as PlayableCharacter).canUsePassive, "battle start should leave guardian passive reusable")
        assertPassiveApplications(
            moment = "battle start",
            character = paladinAtStart,
            expected = listOf(paladinModifier),
        )

        val afterRoundOne = afterBattleStart
            .withPending(
                BattleAction(paladinAtStart, null, PossibleBattleActions.HOLD, Stats(), 1),
                BattleAction(afterBattleStart.battle!!.character("target"), null, PossibleBattleActions.HOLD, Stats(), 1),
            )
            .resolvePending()

        assertPassiveApplications(
            moment = "round 1",
            character = afterRoundOne.battle!!.character("guardian"),
            expected = listOf(
                paladinModifier.copy(duration = 1u),
                paladinModifier.copy(duration = 1u),
            ),
        )
    }

    @Test
    fun `AddActionToPending updater resolves pending and applies attack-conditioned passive immediately`() {
        val attackModifier = StatModifier(Stats(dmg = 3), 0u, ModifierType.TMP_PASSIVE_MODIFIER)
        val attackPassive = Passive { battle ->
            val isAttacking = battle.pending.any { it.origin.name == name && it.action == PossibleBattleActions.ATTACK }
            if (isAttacking) addModifier(attackModifier) as PlayableCharacter else this
        }
        val attacker = passiveCharacter("attacker", attackPassive, stats = Stats(hp = 8, dmg = 2, def = 1, spe = 2))
        val target = passiveCharacter("target", Passive { this }, stats = Stats(hp = 9, dmg = 1, def = 1, spe = 1))
        val readyTarget = BattleAction(target, null, PossibleBattleActions.HOLD, Stats(), 1)
        val game = gameWithBattle(
            Battle(
                characters = listOf(attacker, target),
                currentTurn = 1u,
                phase = BattlePhase.BATTLING,
                pending = listOf(readyTarget),
                itemBet = emptyList(),
            )
        )

        val result = game.gameUpdateByName(
            "AddActionToPending",
            BattleAction(attacker, target, PossibleBattleActions.ATTACK, Stats(), 1),
            emptyList(),
        )

        assertPassiveApplications(
            moment = "AddActionToPending before round 1 actions resolve",
            character = result.battle!!.character("attacker"),
            expected = listOf(attackModifier.copy(duration = 1u)),
        )
    }

    @Test
    fun `resolveBattleEnd removes temporary passive modifiers from board characters`() {
        val passiveModifier = StatModifier(Stats(def = 3), 2u, ModifierType.TMP_PASSIVE_MODIFIER)
        val attackModifier = StatModifier(Stats(hp = -1), 0u, ModifierType.BATTLE_ATTACK)
        val permanentModifier = StatModifier(Stats(hp = 1), 0u, ModifierType.PERMANENT_PASSIVE_MODIFIER)
        val winner = passiveCharacter("winner", Passive { this }, stats = Stats(hp = 5, dmg = 2, def = 1, spe = 2))
            .copy(activeStatModifiers = listOf(passiveModifier, permanentModifier))
        val loser = passiveCharacter("loser", Passive { this }, stats = Stats(hp = 1, dmg = 1, def = 1, spe = 1))
            .copy(activeStatModifiers = listOf(passiveModifier, attackModifier))
        val battle = Battle(
            characters = listOf(winner, loser),
            currentTurn = 2u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        val game = gameWithBattle(
            battle = battle,
            boardCharacters = listOf(winner, loser),
        )

        val result = game.resolveBattleEnd(battle, winner)
        val cleanedWinner = result.board.tiles.first { it.character?.name == "winner" }.character!!
        val cleanedLoser = result.board.tiles.first { it.character?.name == "loser" }.character!!

        assertPassiveApplications(
            moment = "battle end cleanup keeps permanent passive only",
            character = cleanedWinner,
            expected = listOf(permanentModifier),
        )
        assertTrue(
            cleanedLoser.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER || it.type == ModifierType.BATTLE_ATTACK },
            "battle end cleanup should remove temporary passive and battle attack modifiers from loser; actual=${cleanedLoser.activeStatModifiers}",
        )
    }

    private fun passiveCharacter(
        name: String,
        passive: Passive,
        stats: Stats = Stats(hp = 5, dmg = 2, def = 1, spe = 2),
        items: List<Item> = emptyList(),
    ): PlayableCharacter =
        testCharacter(name, stats).copy(passive = passive, items = items)

    private fun waitingBattle(vararg characters: PlayableCharacter): Battle =
        Battle(
            characters = characters.toList(),
            phase = BattlePhase.WAITING,
            pending = characters.map { BattleAction(it, null, PossibleBattleActions.FLEE, Stats(), 1) },
            itemBet = emptyList(),
        )

    private fun gameWithBattle(
        battle: Battle,
        boardCharacters: List<PlayableCharacter> = battle.characters.map { it as PlayableCharacter },
    ): Game =
        testGame(
            players = boardCharacters.mapIndexed { index, character ->
                testPlayer((index + 1).toUInt(), currentCharacter = character.name)
            },
            board = testBoardWith(
                *boardCharacters.mapIndexed { index, character ->
                    testBoardTile(BoardPosition(0, index), character = character)
                }.toTypedArray()
            ),
            battle = battle,
        )

    private fun Game.withPending(vararg actions: BattleAction): Game =
        copy(battle = battle!!.copy(pending = actions.toList()))

    private fun Battle.character(name: String) =
        characters.first { it.name == name }

    private fun assertPassiveApplications(
        moment: String,
        character: isel.pt.cbdcg.domain.game.character.Character,
        expected: List<StatModifier>,
    ) {
        val actual = character.activeStatModifiers.filter {
            it.type == ModifierType.TMP_PASSIVE_MODIFIER || it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER
        }
        assertEquals(
            expected,
            actual,
            "$moment should apply passive modifiers to ${character.name}; actual modifiers=${character.activeStatModifiers}",
        )
    }
}
