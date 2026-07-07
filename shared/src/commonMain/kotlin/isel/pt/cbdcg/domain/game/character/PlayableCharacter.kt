package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.ITEM_CAPACITY
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.Entity
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.dto.CharacterDTO
import isel.pt.cbdcg.dto.EntityDTO
import isel.pt.cbdcg.error.CharacterError
import isel.pt.cbdcg.error.CharacterError.*
import kotlin.math.abs

data class PlayableCharacter(
    override val name: String,
    override val baseStats: Stats,
    override val activeStatModifiers: List<StatModifier> = listOf(),
    override val grade: Grade,
    override val evolution: Evolution?,
    val items: List<Item> = listOf(),
    val maxItems: Int = ITEM_CAPACITY,
    val passive: Passive = NoPassive,
    val canUsePassive:Boolean = true
) : Character {
    override val role: CharacterRole = CharacterRole.PLAYABLE

    override fun addModifier(newStatModifier: StatModifier): Character =
        copy(activeStatModifiers = activeStatModifiers.plus(newStatModifier))
    override fun removeModifier(statModifier: StatModifier): Character =
        copy(activeStatModifiers = activeStatModifiers.minus(statModifier))
    override fun decreaseTileEffectModifiers(): Character =
        copy(activeStatModifiers =
            activeStatModifiers.mapNotNull{ mod ->
                if(mod.type != ModifierType.TILE_EFFECT) mod
                else{
                    val newDuration = mod.duration - 1u
                    if(newDuration <= 0u) null
                    else mod.copy(duration = newDuration)
                }
            }
        )
    override fun increaseInBattleModifierTurn(): Character =
        copy(
            activeStatModifiers = activeStatModifiers.map { mod ->
                if (mod.type.isBattleMod()) mod.copy(duration = mod.duration + 1u)
                else mod
            }
        )

    fun hasUsedPassive(): Character = copy(canUsePassive = false)
    fun resetPassive(): Character = copy(canUsePassive = true)

    override fun removeAllBattleMods(): Character =
        copy(activeStatModifiers = activeStatModifiers.filterNot { it.type.isBattleMod() })
    override fun evolve(battle: Battle?): Character{
        if(evolution == null) return this

        val canEvolve = when(evolution.type){
            EvolutionType.ITEM -> {
                val itemName = (evolution as ItemEvolution).item
                items.any{ it.name == itemName }
            }
            EvolutionType.IN_BATTLE -> {
                if(battle == null) return this
                val condition = (evolution as BattleEvolution).condition

                when(condition){
                    InBattleEvolutionConditions.BLOCK -> {
                        val totalDamageBlocked =
                            battle.actions
                                .flatMap{ it.value }
                                .filter { it.action == PossibleBattleActions.ATTACK && it.target?.name == name }
                                .fold(0) { damageBlocked, mod -> damageBlocked + abs(mod.stats.def) }

                        totalDamageBlocked >= evolution.value
                    }
                    InBattleEvolutionConditions.DAMAGE -> {
                        val totalDamageDealt =
                            battle.actions
                                .flatMap{ it.value }
                                .filter { it.action == PossibleBattleActions.ATTACK && it.origin.name == name }
                                .fold(0) { damageDealt, mod -> damageDealt + abs(mod.stats.hp) }

                        totalDamageDealt >= evolution.value
                    }
                    InBattleEvolutionConditions.REGEN -> TODO()
                    InBattleEvolutionConditions.PERISH -> {
                        val winner = battle.characters.first{ it.adjustStats().hp > 0 }
                        val currentHP = adjustStats().hp

                        currentHP <= 1 && winner.name != name
                    }
                }
            }
            EvolutionType.NR_BATTLES -> {
                if(battle == null) return this
                val condition = (evolution as MultipleBattlesEvolution).condition
                val currentValue = evolution.currentValue

                when(condition){
                    MultipleBattlesEvolutionConditions.WIN -> {
                        val winner = battle.characters.first{ it.adjustStats().hp > 0 }
                        val newCurrentValue =
                            if(winner.name == name) currentValue + 1 else currentValue

                        if(newCurrentValue < evolution.value)
                            return this.copy(evolution = evolution.copy(currentValue = newCurrentValue))
                        else true
                    }
                    MultipleBattlesEvolutionConditions.BATTLE -> {
                        val winner = battle.characters.first{ it.adjustStats().hp > 0 }
                        val currentHP = adjustStats().hp
                        val newCurrentValue =
                            if(winner.name == name || (winner.name != name && currentHP -1 > 0)) currentValue + 1 else currentValue

                        if(newCurrentValue < evolution.value)
                            return this.copy(evolution = evolution.copy(currentValue = newCurrentValue))
                        else true
                    }
                    MultipleBattlesEvolutionConditions.LOSE -> {
                        val winner = battle.characters.first{ it.adjustStats().hp > 0 }
                        val currentHP = adjustStats().hp
                        val newCurrentValue =
                            if(winner.name != name && currentHP - 1 > 0) currentValue + 1 else currentValue

                        if(newCurrentValue < evolution.value)
                            return this.copy(evolution = evolution.copy(currentValue = newCurrentValue))
                        else true
                    }
                    MultipleBattlesEvolutionConditions.FLEE -> {
                        val charactersFled =
                            battle.actions
                                .flatMap { it.value }
                                .filter{ it.action == PossibleBattleActions.FLEE && it.origin.name == name && it.stats.hp < 0 }
                                .map{ it.origin.name }
                        val newCurrentValue =
                            if(name in charactersFled) currentValue + 1 else currentValue

                        if(newCurrentValue < evolution.value)
                            return this.copy(evolution = evolution.copy(currentValue = newCurrentValue))
                        else true
                    }
                }
            }
        }

        return if(canEvolve){
            val baseCharacter = PlayableCharacterCatalog.getCharacterByName(evolution.character)
                ?: throw CharacterDoesNotExist(evolution.character)

            baseCharacter.copy(items = items)
        } else this
    }

    override fun toCharacterDTO(): CharacterDTO =
        CharacterDTO(
            type = "P",
            name = name,
            baseStats = baseStats.toString(),
            activeModifiers = activeStatModifiers.map{ it.toModifierDTO() }.toTypedArray(),
            grade = grade.code(),
            evolution = evolution?.toEvolutionDTO(),
            items = items.map{ it.toItemDTO() }.toTypedArray(),
            maxItems = maxItems
        )

    override fun Entity.toEntityDTO() = EntityDTO( character = toCharacterDTO())
    override fun <T : Entity> toEntity() = this as Entity


}

fun PlayableCharacter.equipItem(item: Item): PlayableCharacter {
    if(items.size >= maxItems)
        throw CharacterError.ItemCapacityLimit(maxItems)

    return copy(items = items + item).evolve() as PlayableCharacter
}
fun PlayableCharacter.unequip(item: Item): PlayableCharacter = copy(items = items - item)

fun CharacterDTO.toPlayableCharacter(): PlayableCharacter =
    PlayableCharacter(
        name = name,
        baseStats = baseStats.toStats(),
        activeStatModifiers = activeModifiers.map{ it.toStatModifier() },
        grade = grade.toGrade(),
        items = items.map{ it.toItem() },
        maxItems = maxItems,
        evolution = evolution?.toEvolution(),
        passive = PlayableCharacterCatalog.getCharacterByName(name)?.passive?:throw CharacterDoesNotExist(name)
    )

fun getPlayableCharacterByName(name: String): PlayableCharacter? = PlayableCharacterCatalog.basicCharacters.find { it.name == name }?: PlayableCharacterCatalog.rareCharacters.find { it.name == name }?: PlayableCharacterCatalog.epicCharacters.find { it.name == name }
object PlayableCharacterCatalog {
    val basicCharacters = listOf(
        PlayableCharacter(name = "alchemist", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC, evolution = MultipleBattlesEvolution(condition = MultipleBattlesEvolutionConditions.WIN, value = 1, character = "plague_doc"), passive = AlchemistBasic),
        PlayableCharacter(name = "apprentice", baseStats = Stats(2, 3, 2, 2), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.DAMAGE, value = 3, character = "mage"), passive = MageBasic),
        PlayableCharacter(name = "beast_warrior", baseStats = Stats(4, 1, 1, 3), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.DAMAGE, value = 3, character = "predator")),
        PlayableCharacter(name = "elf", baseStats = Stats(3, 2, 1, 3), grade = Grade.BASIC, evolution = MultipleBattlesEvolution(condition = MultipleBattlesEvolutionConditions.BATTLE, value = 2, character = "elf_champ"), passive = ElfBasic),
        PlayableCharacter(name = "guardian", baseStats = Stats(3, 2, 3, 1), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.BLOCK, value = 3, character = "paladin"), passive = PaladinBasic),
        PlayableCharacter(name = "juggernaut", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.DAMAGE, value = 3, character = "maniac"), passive = BerserkerBasic),
        PlayableCharacter(name = "ninja", baseStats = Stats(2, 4, 1, 2), grade = Grade.BASIC, evolution = MultipleBattlesEvolution(condition = MultipleBattlesEvolutionConditions.WIN, value = 1, character = "killer"), passive = AssassinBasic),
        PlayableCharacter(name = "nun", baseStats = Stats(3, 1, 1, 3), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.PERISH, value = 1, character = "priestess"), passive = NoPassive),
        PlayableCharacter(name = "taoist", baseStats = Stats(2, 2, 2, 3), grade = Grade.BASIC, evolution = MultipleBattlesEvolution(condition = MultipleBattlesEvolutionConditions.WIN, value = 1, character = "first_rate"), passive = TaoistBasic),
        PlayableCharacter(name = "thief", baseStats = Stats(2, 2, 2, 3), grade = Grade.BASIC, evolution = MultipleBattlesEvolution(condition = MultipleBattlesEvolutionConditions.FLEE, value = 1, character = "vagabond")),
        PlayableCharacter(name = "trainee", baseStats = Stats(3, 2, 2, 2), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.BLOCK, value = 3, character = "knight"), passive = KnightBasic),
        PlayableCharacter(name = "vampire", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC, evolution = BattleEvolution(condition = InBattleEvolutionConditions.REGEN, value = 2, character = "vampire_count")),
    )
    val rareCharacters = listOf(
        PlayableCharacter(name = "elf_champ", baseStats = Stats(3, 4, 2, 2), grade = Grade.RARE, evolution = ItemEvolution(item = "fae", character = "high_elf"), passive = ElfRare),
        PlayableCharacter(name = "first_rate", baseStats = Stats(2, 3, 3, 3), grade = Grade.RARE, evolution = ItemEvolution(item = "jade_sword", character = "sword_emp"), passive = TaoistRare),
        PlayableCharacter(name = "killer", baseStats = Stats(3, 4, 1, 3), grade = Grade.RARE, evolution = ItemEvolution(item = "red_gourd", character = "red_death"), passive = AssassinRare),
        PlayableCharacter(name = "knight", baseStats = Stats(5, 2, 2, 2), grade = Grade.RARE, evolution = ItemEvolution(item = "badge", character = "commander"), passive = KnightRare),
        PlayableCharacter(name = "mage", baseStats = Stats(3, 4, 2, 2), grade = Grade.RARE, evolution = ItemEvolution(item = "golden_glove", character = "archmage"), passive = MageRare),
        PlayableCharacter(name = "maniac", baseStats = Stats(4, 4, 1, 2), grade = Grade.RARE, evolution = ItemEvolution(item = "golden_star", character = "war_god"), passive = BerserkerRare),
        PlayableCharacter(name = "paladin", baseStats = Stats(3, 2, 5, 1), grade = Grade.RARE, evolution = ItemEvolution(item = "peacemaker", character = "god_warrior")),
        PlayableCharacter(name = "plague_doc", baseStats = Stats(4, 3, 2, 2), grade = Grade.RARE, evolution = ItemEvolution(item = "poison_shard", character = "heavenly_doc"), passive = AlchemistRare),
        PlayableCharacter(name = "predator", baseStats = Stats(4, 2, 2, 3), grade = Grade.RARE, evolution = ItemEvolution(item = "unknown_stone", character = "hell_beast"), passive = WerewolfRare),
        PlayableCharacter(name = "priestess", baseStats = Stats(4, 2, 3, 2), grade = Grade.RARE, evolution = ItemEvolution(item = "nirvana_cross", character = "apostle"), passive = PriestRare),
        PlayableCharacter(name = "vagabond", baseStats = Stats(3, 2, 2, 4), grade = Grade.RARE, evolution = ItemEvolution(item = "idol", character = "golden_thief")),
        PlayableCharacter(name = "vampire_count", baseStats = Stats(4, 3, 1, 3), grade = Grade.RARE, evolution = ItemEvolution(item = "chalice", character = "ancestor")),
    )
    val epicCharacters = listOf(
        PlayableCharacter(name = "ancestor", baseStats = Stats(4, 4, 1, 3), grade = Grade.EPIC, evolution = null),
        PlayableCharacter(name = "apostle", baseStats = Stats(4, 3, 3, 2), grade = Grade.EPIC, evolution = null, passive = PriestEpic),
        PlayableCharacter(name = "archmage", baseStats = Stats(3, 4, 2, 3), grade = Grade.EPIC, evolution = null, passive = MageEpic),
        PlayableCharacter(name = "commander", baseStats = Stats(5, 3, 2, 2), grade = Grade.EPIC, evolution = null, passive = KnightEpic),
        PlayableCharacter(name = "god_warrior", baseStats = Stats(4, 2, 5, 1), grade = Grade.EPIC, evolution = null, passive = PaladinEpic),
        PlayableCharacter(name = "golden_thief", baseStats = Stats(3, 2, 2, 5), grade = Grade.EPIC, evolution = null),
        PlayableCharacter(name = "heavenly_doc", baseStats = Stats(4, 4, 2, 2), grade = Grade.EPIC, evolution = null, passive = AlchemistEpic),
        PlayableCharacter(name = "hell_beast", baseStats = Stats(4, 2, 3, 3), grade = Grade.EPIC, evolution = null, passive = WerewolfEpic),
        PlayableCharacter(name = "high_elf", baseStats = Stats(3, 4, 3, 2), grade = Grade.EPIC, evolution = null, passive= ElfEpic),
        PlayableCharacter(name = "red_death", baseStats = Stats(3, 5, 1, 3), grade = Grade.EPIC, evolution = null, passive = AssassinEpic),
        PlayableCharacter(name = "sword_emp", baseStats = Stats(2, 3, 3, 4), grade = Grade.EPIC, evolution = null, passive = TaoistEpic),
        PlayableCharacter(name = "war_god", baseStats = Stats(5, 4, 1, 2), grade = Grade.EPIC, evolution = null, passive = BerserkerEpic),
    )

    fun getCharacterByName(name: String): PlayableCharacter? =
        basicCharacters.find { it.name == name } ?: rareCharacters.find { it.name == name } ?: epicCharacters.find { it.name == name }
}