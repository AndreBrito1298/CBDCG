package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.AOE_EFFECT_MOD
import isel.pt.cbdcg.EFFECT_DURATION
import isel.pt.cbdcg.SINGLE_TARGET_EFFECT_MOD
import isel.pt.cbdcg.domain.game.board.tile.AllTileEffects
import isel.pt.cbdcg.domain.game.board.tile.StatType
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.tile.effectType
import isel.pt.cbdcg.domain.game.board.tile.getStatModifier
import isel.pt.cbdcg.domain.game.board.tile.isPositive
import isel.pt.cbdcg.domain.game.board.tile.toTileEffect
import isel.pt.cbdcg.domain.game.board.tile.toTileEffectDTO
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.error.GameError
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TileEffectTest {

    @Test
    fun `effectType parses non stat tile effects`() {
        assertEquals(TileEffectTypes.None, "None".effectType())
        assertEquals(TileEffectTypes.Start, "Start".effectType())
        assertEquals(TileEffectTypes.Chest, "Chest".effectType())
        assertEquals(TileEffectTypes.BigChest, "BigChest".effectType())
    }

    @Test
    fun `effectType parses every stat effect for every stat`() {
        StatType.entries.forEach { stat ->
            assertEquals(TileEffectTypes.StatUp(stat), "${stat.name}Up".effectType())
            assertEquals(TileEffectTypes.StatDown(stat), "${stat.name}Down".effectType())
            assertEquals(TileEffectTypes.StatUpAoE(stat), "${stat.name}UpAoE".effectType())
            assertEquals(TileEffectTypes.StatDownAoE(stat), "${stat.name}DownAoE".effectType())
        }
    }

    @Test
    fun `effectType throws for invalid effect names`() {
        assertFailsWith<GameError.InvalidFormat> { "HpUp".effectType() }
        assertFailsWith<GameError.InvalidFormat> { "DmgSideways".effectType() }
        assertFailsWith<GameError.InvalidFormat> { "Unknown".effectType() }
    }

    @Test
    fun `tile effect type names match serialized effect names`() {
        StatType.entries.forEach { stat ->
            assertEquals("${stat.name}Up", TileEffectTypes.StatUp(stat).name)
            assertEquals("${stat.name}Down", TileEffectTypes.StatDown(stat).name)
            assertEquals("${stat.name}UpAoE", TileEffectTypes.StatUpAoE(stat).name)
            assertEquals("${stat.name}DownAoE", TileEffectTypes.StatDownAoE(stat).name)
        }

        assertEquals("None", TileEffectTypes.None.name)
        assertEquals("Start", TileEffectTypes.Start.name)
        assertEquals("Chest", TileEffectTypes.Chest.name)
        assertEquals("BigChest", TileEffectTypes.BigChest.name)
    }

    @Test
    fun `isPositive is true only for positive and chest effects`() {
        val positiveTypes = listOf<TileEffectTypes>(
            TileEffectTypes.Chest,
            TileEffectTypes.BigChest,
        ) + StatType.entries.flatMap { stat ->
            listOf(TileEffectTypes.StatUp(stat), TileEffectTypes.StatUpAoE(stat))
        }

        val nonPositiveTypes = listOf<TileEffectTypes>(
            TileEffectTypes.None,
            TileEffectTypes.Start,
        ) + StatType.entries.flatMap { stat ->
            listOf(TileEffectTypes.StatDown(stat), TileEffectTypes.StatDownAoE(stat))
        }

        positiveTypes.forEach { type -> assertTrue(TileEffect(type).isPositive(), "$type should be positive") }
        nonPositiveTypes.forEach { type -> assertFalse(TileEffect(type).isPositive(), "$type should not be positive") }
    }

    @Test
    fun `getStatModifier creates single target stat up modifiers for every stat`() {
        StatType.entries.forEach { stat ->
            val modifier = TileEffect(TileEffectTypes.StatUp(stat)).getStatModifier()

            assertEquals(expectedStats(stat, SINGLE_TARGET_EFFECT_MOD), modifier.stats)
            assertEquals(EFFECT_DURATION, modifier.duration)
            assertEquals(ModifierType.TILE_EFFECT, modifier.type)
        }
    }

    @Test
    fun `getStatModifier creates single target stat down modifiers for every stat`() {
        StatType.entries.forEach { stat ->
            val modifier = TileEffect(TileEffectTypes.StatDown(stat)).getStatModifier()

            assertEquals(expectedStats(stat, -SINGLE_TARGET_EFFECT_MOD), modifier.stats)
            assertEquals(EFFECT_DURATION, modifier.duration)
            assertEquals(ModifierType.TILE_EFFECT, modifier.type)
        }
    }

    @Test
    fun `getStatModifier creates aoe stat up modifiers for every stat`() {
        StatType.entries.forEach { stat ->
            val modifier = TileEffect(TileEffectTypes.StatUpAoE(stat)).getStatModifier()

            assertEquals(expectedStats(stat, AOE_EFFECT_MOD), modifier.stats)
            assertEquals(EFFECT_DURATION, modifier.duration)
            assertEquals(ModifierType.TILE_EFFECT, modifier.type)
        }
    }

    @Test
    fun `getStatModifier creates aoe stat down modifiers for every stat`() {
        StatType.entries.forEach { stat ->
            val modifier = TileEffect(TileEffectTypes.StatDownAoE(stat)).getStatModifier()

            assertEquals(expectedStats(stat, -AOE_EFFECT_MOD), modifier.stats)
            assertEquals(EFFECT_DURATION, modifier.duration)
            assertEquals(ModifierType.TILE_EFFECT, modifier.type)
        }
    }

    @Test
    fun `getStatModifier throws for effects without stat modifiers`() {
        listOf(
            TileEffectTypes.None,
            TileEffectTypes.Start,
            TileEffectTypes.Chest,
            TileEffectTypes.BigChest,
        ).forEach { type ->
            assertFailsWith<GameError.InvalidFormat> { TileEffect(type).getStatModifier() }
        }
    }

    @Test
    fun `tile effect dto roundtrip preserves every effect type and properties`() {
        val effects = listOf(
            TileEffect(TileEffectTypes.None, range = 0u, maxCooldown = 0u, info = "none"),
            TileEffect(TileEffectTypes.Start, range = 0u, maxCooldown = 0u, info = "start"),
            TileEffect(TileEffectTypes.Chest, range = 1u, maxCooldown = 2u, info = "chest"),
            TileEffect(TileEffectTypes.BigChest, range = 2u, maxCooldown = 3u, info = "big chest"),
        ) + StatType.entries.flatMap { stat ->
            listOf(
                TileEffect(TileEffectTypes.StatUp(stat), range = 1u, maxCooldown = 2u, info = "up"),
                TileEffect(TileEffectTypes.StatDown(stat), range = 1u, maxCooldown = 2u, info = "down"),
                TileEffect(TileEffectTypes.StatUpAoE(stat), range = 3u, maxCooldown = 4u, info = "up aoe"),
                TileEffect(TileEffectTypes.StatDownAoE(stat), range = 3u, maxCooldown = 4u, info = "down aoe"),
            )
        }

        effects.forEach { effect ->
            assertEquals(effect, effect.toTileEffectDTO().toTileEffect())
        }
    }

    @Test
    fun `all tile effects catalog contains every playable tile effect with expected copies`() {
        assertEquals(16u, AllTileEffects.allTileEffects.values.sumOf { it.toInt() }.toUInt())
        assertEquals(3u, AllTileEffects.allTileEffects.getValue(TileEffectTypes.Chest.catalogEffect()))
        assertEquals(1u, AllTileEffects.allTileEffects.getValue(TileEffectTypes.BigChest.catalogEffect()))

        StatType.entries.forEach { stat ->
            assertEquals(1u, AllTileEffects.statUp.getValue(TileEffectTypes.StatUp(stat).catalogEffect()))
            assertEquals(1u, AllTileEffects.statDown.getValue(TileEffectTypes.StatDown(stat).catalogEffect()))
            assertEquals(1u, AllTileEffects.statUpAoE.getValue(TileEffectTypes.StatUpAoE(stat).catalogEffect()))
            assertEquals(1u, AllTileEffects.statDownAoE.getValue(TileEffectTypes.StatDownAoE(stat).catalogEffect()))
        }
    }

    @Test
    fun `catalog stat effects use expected ranges and cooldowns`() {
        StatType.entries.forEach { stat ->
            assertEquals(TileEffect(TileEffectTypes.StatUp(stat), maxCooldown = 2u, info = AllTileEffects.statUp.keys.first { it.type == TileEffectTypes.StatUp(stat) }.info), TileEffectTypes.StatUp(stat).catalogEffect())
            assertEquals(1u, TileEffectTypes.StatDown(stat).catalogEffect().range)
            assertEquals(2u, TileEffectTypes.StatDown(stat).catalogEffect().maxCooldown)
            assertEquals(3u, TileEffectTypes.StatUpAoE(stat).catalogEffect().range)
            assertEquals(4u, TileEffectTypes.StatUpAoE(stat).catalogEffect().maxCooldown)
            assertEquals(3u, TileEffectTypes.StatDownAoE(stat).catalogEffect().range)
            assertEquals(4u, TileEffectTypes.StatDownAoE(stat).catalogEffect().maxCooldown)
        }
    }

    private fun expectedStats(stat: StatType, value: Int): Stats =
        Stats(
            dmg = if (stat == StatType.Dmg) value else 0,
            def = if (stat == StatType.Def) value else 0,
            spe = if (stat == StatType.Spe) value else 0,
        )

    private fun TileEffectTypes.catalogEffect(): TileEffect =
        AllTileEffects.allTileEffects.keys.first { it.type == this }
}
