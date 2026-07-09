package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.BattlePhase
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog.getCharacterByName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

/*
 * NOTE ON UNVERIFIED SIGNATURES:
 * These tests were written without access to the actual compiled definitions of
 * Battle, Stats, StatModifier, ModifierType, or BattleAction beyond what is
 * inferable from the pasted source files. Field names/order below (especially
 * for Stats(hp, dmg, def, spe) and Battle(...)) are best-effort inferences and
 * may need small adjustments to compile against your real source.
 */

// ---- Test helpers -----------------------------------------------------------

/** Builds a minimal Battle containing the given characters, defaulting to empty pending/actions. */
private fun testBattle(
    characters: List<Character>,
    pending: List<BattleAction> = emptyList(),
    currentTurn: UInt = 1u,
    actions: Map<UInt, List<BattleAction>> = emptyMap(),
): Battle =
    Battle(
        phase = BattlePhase.BATTLING,
        characters = characters,
        itemBet = emptyList(),
        pending = pending,
        actions = actions,
        currentTurn = currentTurn,
    )

/** Applies a BATTLE_ATTACK modifier to a character to simulate "was attacked and lost X hp". */
private fun Character.withAttackDamage(hpLost: Int): Character =
    this.addModifier(
        StatModifier(
            stats = Stats(hp = -hpLost),
            duration = 1u,
            type = ModifierType.BATTLE_ATTACK,
        )
    )

/** Applies a BATTLE_ATTACK modifier reducing def to simulate "was damaged" (def-only damage). */
private fun Character.withDefDamage(defLost: Int): Character =
    this.addModifier(
        StatModifier(
            stats = Stats(def = -defLost),
            duration = 1u,
            type = ModifierType.BATTLE_ATTACK,
        )
    )

/** Finds a character by name in a battle's character list, failing the test if not found. */
private fun Battle.charByName(name: String): Character =
    characters.find { it.name == name } ?: error("Character $name not found in battle")

/** Runs a character's passive against a battle, returning the resulting Battle or Character. */
@Suppress("UNCHECKED_CAST")
private fun <T> Character.runPassive(battle: Battle?): T =
    (passiveProps.passive as Passive<T>).run { usePassive(battle) }

private fun BattleAction(
    origin: Character,
    target: Character?,
    action: PossibleBattleActions,
    stats: Stats = Stats(),
    turn: Int = 1,
) = isel.pt.cbdcg.domain.game.BattleAction(origin, target, action, stats, turn)


// ---- Knight -----------------------------------------------------------------

class KnightPassiveTests {

    @Test
    fun `KnightBasic gains +def for 2 turns when damaged`() {
        val knight = getCharacterByName("trainee")!!.withDefDamage(1)
        val ally = getCharacterByName("guardian")!!
        val battle = testBattle(listOf(knight, ally))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("trainee")

        val added = updatedKnight.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added, "Expected a TMP_PASSIVE_MODIFIER to be added")
        assertEquals(1, added.stats.def)
        assertEquals(2u, added.duration)
    }

    @Test
    fun `KnightBasic does nothing when not damaged`() {
        val knight = getCharacterByName("trainee")!!
        val battle = testBattle(listOf(knight))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("trainee")

        assertTrue(updatedKnight.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }

    @Test
    fun `KnightBasic does nothing when passive already used`() {
        val knight = (getCharacterByName("trainee")!!.withDefDamage(1)).hasUsedPassive()
        val battle = testBattle(listOf(knight))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("trainee")

        assertTrue(updatedKnight.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }

    @Test
    fun `KnightRare gains ++def for 2 turns when hp is lost`() {
        val knight = getCharacterByName("knight")!!.withAttackDamage(2)
        val battle = testBattle(listOf(knight))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("knight")

        val added = updatedKnight.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(2, added.stats.def)
        assertEquals(2u, added.duration)
    }

    @Test
    fun `KnightRare does nothing when hp was not lost (only def damage)`() {
        val knight = getCharacterByName("knight")!!.withDefDamage(1)
        val battle = testBattle(listOf(knight))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("knight")

        assertTrue(updatedKnight.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }

    @Test
    fun `KnightEpic recovers hp so net loss from the attack is exactly -2`() {
        // Attack dealt 10 damage; expected overflow recovery = -2 - (-10) = 8
        val knight = getCharacterByName("commander")!!.withAttackDamage(10)
        val battle = testBattle(listOf(knight))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("commander")

        val recoveryMod = updatedKnight.activeStatModifiers
            .find { it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER }
        assertNotNull(recoveryMod, "Expected a PERMANENT_PASSIVE_MODIFIER recovering hp")
        assertEquals(8, recoveryMod.stats.hp)

        // net effect: original attack (-10) + recovery (+8) == -2
        val originalLoss = updatedKnight.activeStatModifiers
            .find { it.type == ModifierType.BATTLE_ATTACK }!!.stats.hp
        assertEquals(-2, originalLoss + recoveryMod.stats.hp)
    }

    @Test
    fun `KnightEpic does nothing when hp was not lost`() {
        val knight = getCharacterByName("commander")!!
        val battle = testBattle(listOf(knight))

        val result = knight.runPassive<Battle>(battle)
        val updatedKnight = result.charByName("commander")

        assertTrue(updatedKnight.activeStatModifiers.none { it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER })
    }
}

// ---- Mage ---------------------------------------------------------------

class MagePassiveTests {

    @Test
    fun `MageBasic gains +dmg when attacking`() {
        val mage = getCharacterByName("apprentice")!!
        val target = getCharacterByName("guardian")!!
        val attackAction = BattleAction(mage, target, PossibleBattleActions.ATTACK)
        val battle = testBattle(listOf(mage, target), pending = listOf(attackAction))

        val result = mage.runPassive<Battle>(battle)
        val updatedMage = result.charByName("apprentice")

        val added = updatedMage.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.dmg)
    }

    @Test
    fun `MageBasic does nothing when not attacking`() {
        val mage = getCharacterByName("apprentice")!!
        val battle = testBattle(listOf(mage), pending = emptyList())

        val result = mage.runPassive<Battle>(battle)
        val updatedMage = result.charByName("apprentice")

        assertTrue(updatedMage.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }

    @Test
    fun `MageRare gains +dmg for 2 turns when attacking`() {
        val mage = getCharacterByName("mage")!!
        val target = getCharacterByName("guardian")!!
        val attackAction = BattleAction(mage, target, PossibleBattleActions.ATTACK)
        val battle = testBattle(listOf(mage, target), pending = listOf(attackAction))

        val result = mage.runPassive<Battle>(battle)
        val updatedMage = result.charByName("mage")

        val added = updatedMage.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.dmg)
        assertEquals(2u, added.duration)
    }

    @Test
    fun `MageEpic gains ++dmg when attacking`() {
        val mage = getCharacterByName("archmage")!!
        val target = getCharacterByName("guardian")!!
        val attackAction = BattleAction(mage, target, PossibleBattleActions.ATTACK)
        val battle = testBattle(listOf(mage, target), pending = listOf(attackAction))

        val result = mage.runPassive<Battle>(battle)
        val updatedMage = result.charByName("archmage")

        val added = updatedMage.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(2, added.stats.dmg)
    }

    @Test
    fun `MageEpic does nothing when not attacking`() {
        val mage = getCharacterByName("archmage")!!
        val battle = testBattle(listOf(mage), pending = emptyList())

        val result = mage.runPassive<Battle>(battle)
        val updatedMage = result.charByName("archmage")

        assertTrue(updatedMage.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }
}

// ---- Assassin -------------------------------------------------------------

class AssassinPassiveTests {

    @Test
    fun `AssassinBasic gains +spe on turn 1 when highest SPD`() {
        // ninja base spe (2nd stat position in Stats(2,4,1,2)) is higher than guardian's
        val ninja = getCharacterByName("ninja")!!
        val guardian = getCharacterByName("guardian")!!
        val battle = testBattle(listOf(ninja, guardian), currentTurn = 1u)

        val result = ninja.runPassive<Battle>(battle)
        val updated = result.charByName("ninja")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.spe)
    }

    @Test
    fun `AssassinBasic gains +def on turn 1 when not highest SPD`() {
        val ninja = getCharacterByName("ninja")!!
        // a faster ally forces ninja to not have the highest SPD
        val fasterAlly = getCharacterByName("killer")!!
        val battle = testBattle(listOf(ninja, fasterAlly), currentTurn = 1u)

        val result = ninja.runPassive<Battle>(battle)
        val updated = result.charByName("ninja")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.def)
    }

    @Test
    fun `AssassinBasic does not activate outside turn 1`() {
        val ninja = getCharacterByName("ninja")!!
        val battle = testBattle(listOf(ninja), currentTurn = 2u)

        val result = ninja.runPassive<Battle>(battle)
        val updated = result.charByName("ninja")

        assertTrue(updated.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }

    @Test
    fun `AssassinEpic gains stat boosts when below max hp`() {
        val killer = getCharacterByName("red_death")!!.withAttackDamage(1)
        val battle = testBattle(listOf(killer))

        val result = killer.runPassive<Battle>(battle)
        val updated = result.charByName("red_death")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.dmg)
    }

    @Test
    fun `AssassinEpic applies an empty modifier when at full hp`() {
        val killer = getCharacterByName("killer")!!.addModifier(StatModifier(Stats(-1), 1u, ModifierType.BATTLE_ATTACK))
        val battle = testBattle(listOf(killer))

        val result = killer.runPassive<Battle>(battle)
        val updated = result.charByName("killer")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(0, added.stats.dmg)
        assertEquals(0, added.stats.def)
        assertEquals(0, added.stats.spe)
    }
}

// ---- Alchemist -------------------------------------------------------------

class AlchemistPassiveTests {

    @Test
    fun `AlchemistBasic gains +dmg on turn 1 when highest hp`() {
        val alchemist = getCharacterByName("alchemist")!!
        val guardian = getCharacterByName("guardian")!!
        val battle = testBattle(listOf(alchemist, guardian), currentTurn = 1u)

        val result = alchemist.runPassive<Battle>(battle)
        val updated = result.charByName("alchemist")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.dmg)
    }

    @Test
    fun `AlchemistRare recovers hp when damaged, more when far from max hp`() {
        val alchemist = getCharacterByName("plague_doc")!!.withAttackDamage(3)
        val battle = testBattle(listOf(alchemist))

        val result = alchemist.runPassive<Battle>(battle)
        val updated = result.charByName("plague_doc")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(2, added.stats.hp)
    }

    @Test
    fun `AlchemistRare does nothing when not damaged`() {
        val alchemist = getCharacterByName("plague_doc")!!
        val battle = testBattle(listOf(alchemist))

        val result = alchemist.runPassive<Battle>(battle)
        val updated = result.charByName("plague_doc")

        assertTrue(updated.activeStatModifiers.none { it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER })
    }

    @Test
    fun `AlchemistEpic gains all stats when damaged`() {
        val alchemist = getCharacterByName("heavenly_doc")!!.withDefDamage(1)
        val battle = testBattle(listOf(alchemist))

        val result = alchemist.runPassive<Battle>(battle)
        val updated = result.charByName("heavenly_doc")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.def)
        assertEquals(1, added.stats.spe)
        assertEquals(1, added.stats.dmg)
        assertEquals(1, added.stats.hp)
    }

    @Test
    fun `AlchemistEpic does nothing when not damaged`() {
        val alchemist = getCharacterByName("heavenly_doc")!!
        val battle = testBattle(listOf(alchemist))

        val result = alchemist.runPassive<Battle>(battle)
        val updated = result.charByName("heavenly_doc")

        assertTrue(updated.activeStatModifiers.none { it.type == ModifierType.PERMANENT_PASSIVE_MODIFIER })
    }
}

// ---- Elf --------------------------------------------------------------------

class ElfPassiveTests {

    @Test
    fun `ElfBasic gains +def on turn 1 when highest dmg`() {
        val elf = getCharacterByName("elf")!!
        val weakAlly = getCharacterByName("nun")!!
        val battle = testBattle(listOf(elf, weakAlly), currentTurn = 1u)

        val result = elf.runPassive<Battle>(battle)
        val updated = result.charByName("elf")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.def)
    }

    @Test
    fun `ElfRare gains +spe for 2 turns when attacking`() {
        val elf = getCharacterByName("elf_champ")!!
        val target = getCharacterByName("guardian")!!
        val attackAction = BattleAction(elf, target, PossibleBattleActions.ATTACK)
        val battle = testBattle(listOf(elf, target), pending = listOf(attackAction), currentTurn = 1u)

        val result = elf.runPassive<Battle>(battle)
        val updated = result.charByName("elf_champ")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.spe)
        assertEquals(2u, added.duration)
    }

    @Test
    fun `ElfEpic gains ++def for 2 turns on turn 1 when hp is lost`() {
        val elf = getCharacterByName("high_elf")!!.withAttackDamage(1)
        val battle = testBattle(listOf(elf), currentTurn = 1u)

        val result = elf.runPassive<Battle>(battle)
        val updated = result.charByName("high_elf")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(2, added.stats.def)
        assertEquals(2u, added.duration)
    }

    @Test
    fun `ElfEpic does nothing when hp is not lost`() {
        val elf = getCharacterByName("high_elf")!!
        val battle = testBattle(listOf(elf), currentTurn = 1u)

        val result = elf.runPassive<Battle>(battle)
        val updated = result.charByName("high_elf")

        assertTrue(updated.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }
}

// ---- Priest -----------------------------------------------------------------

class PriestPassiveTests {

    @Test
    fun `PriestRare gains all stats for 1 turn`() {
        val priest = getCharacterByName("priestess")!!
        val battle = testBattle(listOf(priest))

        val result = priest.runPassive<Battle>(battle)
        val updated = result.charByName("priestess")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.hp)
        assertEquals(1, added.stats.dmg)
        assertEquals(1, added.stats.def)
        assertEquals(1, added.stats.spe)
        assertEquals(1u, added.duration)
    }

    @Test
    fun `PriestEpic reduces attacker's dmg toward 1 when priest is attacked`() {
        val priest = getCharacterByName("apostle")!!
        val attacker = getCharacterByName("guardian")!!
        val attackAction = BattleAction(attacker, priest, PossibleBattleActions.ATTACK)
        val battle = testBattle(listOf(priest, attacker), pending = listOf(attackAction))

        val result = priest.runPassive<Battle>(battle)
        val updatedAttacker = result.charByName("guardian")

        val added = updatedAttacker.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added, "Expected the attacker to receive a dmg-reducing modifier")

        val expectedMod = 1 - attacker.adjustStats().dmg
        assertEquals(-expectedMod, added.stats.dmg)
    }
}

// ---- Werewolf (non-item epic only) ------------------------------------------

class WerewolfPassiveTests {

    @Test
    fun `WerewolfEpic reduces target's def by target's base def when attacking`() {
        val werewolf = getCharacterByName("hell_beast")!!
        val target = getCharacterByName("guardian")!!
        val attackAction = BattleAction(werewolf, target, PossibleBattleActions.ATTACK)
        val battle = testBattle(listOf(werewolf, target), pending = listOf(attackAction))

        val result = werewolf.runPassive<Battle>(battle)
        val updatedTarget = result.charByName("guardian")

        val added = updatedTarget.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(-target.baseStats.def, added.stats.def)
    }

    @Test
    fun `WerewolfEpic does nothing when werewolf is not attacking`() {
        val werewolf = getCharacterByName("hell_beast")!!
        val battle = testBattle(listOf(werewolf), pending = emptyList())

        val result = werewolf.runPassive<Battle>(battle)
        val updated = result.charByName("hell_beast")

        assertTrue(updated.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }
}

// ---- Berserker (non-item epic only) -----------------------------------------

class BerserkerPassiveTests {

    @Test
    fun `BerserkerEpic applies dmg modifier to target based on parity of hp loss`() {
        val berserker = getCharacterByName("war_god")!!
        val target = getCharacterByName("guardian")!!
        // hp = -4 (even damage) as part of the attack action's own stats
        val attackAction = BattleAction(berserker, target, PossibleBattleActions.ATTACK, stats = Stats(hp = -4))
        val battle = testBattle(listOf(berserker, target), pending = listOf(attackAction))

        val result = berserker.runPassive<Battle>(battle)
        val updatedTarget = result.charByName("guardian")

        val added = updatedTarget.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(-(-4 % 2), added.stats.dmg)
    }

    @Test
    fun `BerserkerEpic does nothing when berserker did not attack`() {
        val berserker = getCharacterByName("war_god")!!
        val battle = testBattle(listOf(berserker), pending = emptyList())

        val result = berserker.runPassive<Battle>(battle)
        val updated = result.charByName("war_god")

        assertTrue(updated.activeStatModifiers.none { it.type == ModifierType.TMP_PASSIVE_MODIFIER })
    }
}

// ---- Thief ------------------------------------------------------------------

class ThiefPassiveTests {

    @Test
    fun `ThiefBasic gains +spe for 1 turn when undamaged`() {
        val thief = getCharacterByName("thief")!!
        val battle = testBattle(listOf(thief))

        val result = thief.runPassive<Battle>(battle)
        val updated = result.charByName("thief")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.spe)
        assertEquals(1u, added.duration)
    }

    @Test
    fun `ThiefRare gains ++def for 2 turns when damaged instead of spe`() {
        val thief = getCharacterByName("vagabond")!!.withDefDamage(1)
        val battle = testBattle(listOf(thief))

        val result = thief.runPassive<Battle>(battle)
        val updated = result.charByName("vagabond")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(2, added.stats.def)
        assertEquals(2u, added.duration)
    }

    @Test
    fun `ThiefRare gains +spe for 1 turn when undamaged`() {
        val thief = getCharacterByName("vagabond")!!
        val battle = testBattle(listOf(thief))

        val result = thief.runPassive<Battle>(battle)
        val updated = result.charByName("vagabond")

        val added = updated.activeStatModifiers.find { it.type == ModifierType.TMP_PASSIVE_MODIFIER }
        assertNotNull(added)
        assertEquals(1, added.stats.spe)
        assertEquals(1u, added.duration)
    }
}

// ---- No Passive ---------------------------------------------------------------

class NoPassiveTests {

    @Test
    fun `NoPassive returns the character unchanged`() {
        val nun = getCharacterByName("nun")!!
        val battle = testBattle(listOf(nun))

        val result = nun.runPassive<Battle>(battle)
        val updated = result.charByName("nun")

        assertEquals(nun.activeStatModifiers, updated.activeStatModifiers)
    }
}