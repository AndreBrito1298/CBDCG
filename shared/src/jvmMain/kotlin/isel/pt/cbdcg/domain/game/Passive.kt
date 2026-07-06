
/*package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.error.BattleError
import isel.pt.cbdcg.error.CharacterError
import isel.pt.cbdcg.error.GameError
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import java.lang.reflect.Field
import java.lang.reflect.Modifier

fun interface Passive{
    fun PlayableCharacter.tryActivate(battle: Battle): PlayableCharacter

    fun PlayableCharacter.usePassive(battle: Battle): PlayableCharacter =
        this.tryActivate(battle)

}

fun Passive.usePassive(character: PlayableCharacter, battle: Battle): PlayableCharacter {
    return this@usePassive.run { character.usePassive(battle) }
}

enum class StatName{
    SPD,
    HP,
    DMG,
    DEF
}

private fun Character.hasAttacked(action: BattleAction) =
    this == action.origin && action.action == PossibleBattleActions.ATTACK

private fun Character.hasBeenAttacked():Boolean{
    this.activeStatModifiers.forEach { modifier ->
        if(modifier.type == ModifierType.BATTLE_ATTACK && modifier.stats.hp < 0) return true
    }
    return false
}


private fun Character.isCorrectItemType(stat: StatName): Boolean {

    return false
}

private fun Battle.hasHighestStat(self: PlayableCharacter, stat: StatName): Boolean{
    return when(stat){
        StatName.SPD -> (this.characters.maxBy { it.adjustStats().spe } == self)
        StatName.HP -> (this.characters.maxBy { it.adjustStats().hp } == self)
        StatName.DMG -> (this.characters.maxBy { it.adjustStats().dmg } == self)
        StatName.DEF -> (this.characters.maxBy { it.adjustStats().def } == self)
    }
}

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD)
annotation class RegisterPassive(val name: String)

private val passiveByName: Map<String, Passive> by lazy {
    val reflections = Reflections("isel.pt.cbdcg.domain.game.character", Scanners.FieldsAnnotated)
    reflections.getFieldsAnnotatedWith(RegisterPassive::class.java)
        .mapNotNull { field: Field ->
            if (!Modifier.isStatic(field.modifiers)) return@mapNotNull null
            field.isAccessible = true
            val instance = field.get(null) as? Passive ?: return@mapNotNull null
            val name = field.getAnnotation(RegisterPassive::class.java).name
            name to instance
        }.toMap()
}

fun getPassiveByName(name: String): Passive =
    passiveByName[name] ?: throw CharacterError.CharacterDoesNotExist(name)



@field:RegisterPassive("KnightBasic")
val KnightBasic: Passive = { battle ->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(def = 1), 2u, ModifierType.PASSIVE_MODIFIER)
    self = hasUsedPassive()
    if(self.hasBeenAttacked()) self.addModifier(passiveModifier)
    self as PlayableCharacter
}

@field:RegisterPassive("KnightRare")
val KnightRare: Passive = { battle ->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(def = 2), 2u, ModifierType.PASSIVE_MODIFIER)
    self = hasUsedPassive()
    if(self.hasBeenAttacked()) self.addModifier(passiveModifier)
    self as PlayableCharacter
}


@field:RegisterPassive("MageBasic")
val MageBasic: Passive =  { battle ->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(dmg = 1), 0u, ModifierType.PASSIVE_MODIFIER)
    battle.pending.forEach { action->
        if(this.hasAttacked(action)) {
            self  = self.addModifier(passiveModifier)
            hasUsedPassive()
        }
    }
    self as PlayableCharacter
}

@field:RegisterPassive("MageEpic")
val MageEpic: Passive = { battle->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(dmg = 2), 0u, ModifierType.PASSIVE_MODIFIER)
    battle.pending.forEach { actions->
        if(self.hasAttacked(actions)) {
            self  = self.addModifier(passiveModifier)
        }
    }
    self as PlayableCharacter
}

@field:RegisterPassive("AssassinBasic")
val AssassinBasic: Passive = { battle->
    var self =battle.findSelf(this.name)
    val passiveModifier = if(battle.hasHighestStat(self as PlayableCharacter, StatName.SPD))
        StatModifier(Stats(spe = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    else StatModifier(Stats(def = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    self = hasUsedPassive()
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("AssassinRare")
val AssassinRare: Passive = { battle->
    var self = battle.findSelf(this.name)
    val passiveModifier = if(battle.hasHighestStat(self as PlayableCharacter, StatName.HP))
        StatModifier(Stats(dmg = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    else StatModifier(Stats(def = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    self = hasUsedPassive()
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("AlchemistBasic")
val AlchemistBasic: Passive = {battle->
    var self = battle.findSelf(this.name)
    resetPassive()
    val passiveModifier = if(battle.hasHighestStat(self as PlayableCharacter, StatName.HP))
        StatModifier(Stats(dmg = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    else StatModifier(Stats(def = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    self = hasUsedPassive()
    self.addModifier(passiveModifier) as PlayableCharacter
}


@field:RegisterPassive("AlchemistRare")
val AlchemistRare: Passive = {battle->
    var self = battle.findSelf(this.name)
    val passiveModifier = if(self.adjustStats().hp+2 > self.baseStats.hp) StatModifier(Stats(hp = 1), 0u, ModifierType.PERMANENT)
    else StatModifier(Stats(hp = 2), 1u, ModifierType.PERMANENT)
    if(self.hasBeenAttacked()){
        self = hasUsedPassive()
        self.addModifier(passiveModifier)
    }
    self as PlayableCharacter
}

@field:RegisterPassive("AlchemistEpic")
val AlchemistEpic: Passive = {battle->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(def = 1, spe = 1, dmg = 1, hp = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    if(self.hasBeenAttacked()){
        self = hasUsedPassive()
        self.addModifier(passiveModifier)
    }
    self as PlayableCharacter
}

@field:RegisterPassive("PaladinBasic")
val PaladinBasic: Passive = {battle ->
    var self = battle.findSelf(this.name)
    var modVal = 0
    (self as PlayableCharacter).items.forEach { item ->
        modVal += item.stats.def
    }
    val passiveModifier = StatModifier(Stats(dmg = modVal, def = -modVal), 0u, ModifierType.PASSIVE_MODIFIER)
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("PaladinEpic")
val PaladinEpic: Passive = {battle ->
    var self = battle.findSelf(this.name)
    var modVal = 0
    (self as PlayableCharacter).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(hp = modVal), 0u, ModifierType.PASSIVE_MODIFIER)
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("ElfBasic")
val ElfBasic: Passive = {battle ->
    var self = battle.findSelf(this.name)
    self = hasUsedPassive()
    val passiveModifier = if(battle.hasHighestStat(self as PlayableCharacter, StatName.DMG))
        StatModifier(Stats(def = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    else StatModifier(Stats(spe = 1), 1u, ModifierType.PASSIVE_MODIFIER)
    self = hasUsedPassive()
    self = self.addModifier(passiveModifier) as PlayableCharacter
    self
}

@field:RegisterPassive("ElfRare")
val ElfRare: Passive = {battle->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(spe = 1), 2u, ModifierType.PASSIVE_MODIFIER)
    battle.pending.forEach { actions->
        if(self.hasAttacked(actions)) {
            self  = self.addModifier(passiveModifier)
            self = hasUsedPassive()
        }
    }
    self as PlayableCharacter
}

@field:RegisterPassive("ElfEpic")
val ElfEpic: Passive = {battle->
    var self = battle.findSelf(this.name)
    val passiveModifier = StatModifier(Stats(def = 1), 2u, ModifierType.PASSIVE_MODIFIER)
    if(self.hasBeenAttacked()){
        self = self.addModifier(passiveModifier)
    }
    self as PlayableCharacter
}

@field:RegisterPassive("WerewolBasic")
val WerewolBasic: Passive = {battle->
    var self = battle.findSelf(this.name)
    if(canUsePassive){
        TODO()
    }
    else{
        TODO()
    }
}

@field:RegisterPassive("TaoistBasic")
val TaoistBasic:Passive = {battle->
    var self = battle.findSelf(this.name)
    var mod = 0
    this.items.forEach { item ->
        mod+=item.stats.spe
    }
    val passiveModifier = StatModifier(Stats(spe=-mod, dmg=mod), 0u, ModifierType.PASSIVE_MODIFIER)
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("TaoistRare")
val TaoistRare:Passive = {battle->
    var self = battle.findSelf(this.name)
    if(this.items.isEmpty()){
        val passiveModifier = StatModifier(Stats(dmg=1, spe=1), 0u, ModifierType.PASSIVE_MODIFIER)
        self.addModifier(passiveModifier) as PlayableCharacter
    }
    self as PlayableCharacter
}

@field:RegisterPassive("TaoistEpic")
val TaoistEpic: Passive = {battle->
    var self = battle.findSelf(this.name)
    if(this.items.isEmpty()){
        val passiveModifier = StatModifier(Stats(def=1,dmg=1, spe=1), 0u, ModifierType.PASSIVE_MODIFIER)
        self.addModifier(passiveModifier) as PlayableCharacter
    }
    self as PlayableCharacter
}



@field:RegisterPassive("WerewolfRare")
val WerewolfRare: Passive = {battle->
    var self = battle.findSelf(this.name)
    var modVal = 0
    (self as PlayableCharacter).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(dmg = modVal), 0u, ModifierType.PASSIVE_MODIFIER)
    self = self.addModifier(passiveModifier) as PlayableCharacter
    self as PlayableCharacter
}


@field:RegisterPassive("BerserkerBasic")
val BerserkerBasic: Passive = {battle ->
    var self = battle.findSelf(this.name)
    var modVal = 0
    (self as PlayableCharacter).items.forEach { item ->
        modVal += item.stats.dmg
    }
    val passiveModifier = StatModifier(Stats(dmg = -modVal, def = modVal), 0u, ModifierType.PASSIVE_MODIFIER)
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("BerserkerRare")
val BerserkerRare: Passive = {battle ->
    var self = battle.findSelf(this.name)
    var modVal = 0
    (self as PlayableCharacter).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(def = modVal), 0u, ModifierType.PASSIVE_MODIFIER)
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("BerserkerEpic")
val BerserkerEpic: Passive = {battle->
    var self = battle.findSelf(this.name)
    battle.pending.forEach { action->
        if(this.hasAttacked(action)) {
            val passiveModifier = StatModifier(Stats(dmg = -action.stats.hp%2), 1u, ModifierType.PASSIVE_MODIFIER)
            self  = action.target?.addModifier(passiveModifier)?:throw BattleError.InvalidAction("No target in attack")
        }
    }
    self as PlayableCharacter
}

@field:RegisterPassive("PriestRare")
val PriestRare: Passive = {battle->
    var self = battle.findSelf(this.name)
    self = hasUsedPassive()
    val passiveModifier = StatModifier(Stats(1,1,1,1), 1u, ModifierType.PASSIVE_MODIFIER)
    self.addModifier(passiveModifier) as PlayableCharacter
}

@field:RegisterPassive("NoPassive")
val NoPassive: Passive = { battle->
    battle.findSelf(this.name) as PlayableCharacter
}



private fun Battle.findSelf(name: String) =
    this.characters.find { it.name == name }?: throw GameError.NoActiveCharacters()
*/