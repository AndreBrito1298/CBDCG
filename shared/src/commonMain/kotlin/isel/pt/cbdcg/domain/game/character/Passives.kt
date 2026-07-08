package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.ITEM_CAPACITY
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.Entity
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.addToAction
import isel.pt.cbdcg.domain.game.replaceChar
import isel.pt.cbdcg.error.BattleError
import isel.pt.cbdcg.error.GameError
import kotlin.random.Random


enum class PassiveType{
    BATTLE_PASSIVE,
    NEUTRAL_PASSIVE
}

data class PassiveProps(
    val type: PassiveType,
    val isAfterActions: Boolean,
    val description: String,
    val passive: Passive<*>
)

class passivesB(
    val type: PassiveType,
    val passive: Passive<*>
){

}

fun interface Passive<T: Entity>{
    fun Character.tryActivate(battle: Battle?): T
    fun Character.usePassive(battle: Battle?):T{
        println("Trying to use ${this.name} passive")
        return this.tryActivate(battle)
    }
}

enum class StatName{
    SPD,
    HP,
    DMG,
    DEF
}

private fun Character.hasAttacked(action: BattleAction) =
    this.name == action.origin.name && action.action == PossibleBattleActions.ATTACK

private fun Character.hasLostHP():Boolean{
    this.activeStatModifiers.forEach { modifier ->
        if(modifier.type == ModifierType.BATTLE_ATTACK && modifier.stats.hp < 0) return true
    }
    return false
}

private fun Character.hasBeenDamaged():Boolean{
    this.activeStatModifiers.forEach { modifier ->
        if(modifier.type == ModifierType.BATTLE_ATTACK && (modifier.stats.def < 0 || modifier.stats.hp < 0)) return true
    }
    return false
}

private fun Battle.hasHighestStat(self: Character, stat: StatName): Boolean{
   return when(stat){
        StatName.SPD -> (this.characters.maxBy { it.adjustStats().spe } == self)
        StatName.HP -> (this.characters.maxBy { it.adjustStats().hp } == self)
        StatName.DMG -> (this.characters.maxBy { it.adjustStats().dmg } == self)
        StatName.DEF -> (this.characters.maxBy { it.adjustStats().def } == self)
    }
}



val KnightBasic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(this.canUsePassive){
        val statMod = Stats(def = 1)
        val passiveModifier = StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = hasUsedPassive()
        if(self.hasBeenDamaged()){
            self = self.addModifier(passiveModifier)
            resBattle = battle.replaceChar(self).
            addToAction(
                BattleAction(self, null, PossibleBattleActions.PASSIVE,statMod, battle.currentTurn.toInt()))
        }
    }
    resBattle
}

val KnightRare: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(this.canUsePassive){
        val statMod = Stats(def = 2)
        val passiveModifier = StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = hasUsedPassive()
        if(self.hasLostHP()){
            self = self.addModifier(passiveModifier)
            resBattle = battle.replaceChar(self).
            addToAction(
                BattleAction(self, null, PossibleBattleActions.PASSIVE,statMod, battle.currentTurn.toInt()))

        }
         }
    resBattle
}

val KnightEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (self.hasLostHP()) {
        val recovery = self.activeStatModifiers.find { it.type == ModifierType.BATTLE_ATTACK }!!.stats.hp
        val overflowRecovery = -2 - recovery
        val statMod = Stats(hp = overflowRecovery)
        val passiveModifier = StatModifier(
            statMod, 5u, ModifierType.PERMANENT_PASSIVE_MODIFIER)
        self = self.addModifier(passiveModifier)
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE,statMod, battle.currentTurn.toInt()))
    }
    resBattle
}

val MageBasic: Passive<Battle> =  { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(this.canUsePassive){
        val statMod = Stats(dmg = 1)
        val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
        battle.pending.forEach { action->
            if(this.hasAttacked(action)) {
                self = hasUsedPassive().addModifier(passiveModifier)
                resBattle = battle.replaceChar(self).
                addToAction(
                    BattleAction(self, null, PossibleBattleActions.PASSIVE,statMod, battle.currentTurn.toInt()))
            }
        }
    }
    resBattle?: battle
}

val MageRare: Passive<Battle> =  { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(this.canUsePassive){
        val statMod = Stats(dmg = 1)
        val passiveModifier = StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER)
        battle.pending.forEach { action->
            if(this.hasAttacked(action)) {
                self = hasUsedPassive().addModifier(passiveModifier)
                resBattle = battle.replaceChar(self).
                addToAction(
                    BattleAction(self, null, PossibleBattleActions.PASSIVE,statMod, battle.currentTurn.toInt()))
            }
        }

    }
    resBattle?: battle

}
val MageEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive) {
        val statMod = Stats(dmg = 2)
        val passiveModifier = StatModifier(statMod, 0u, ModifierType.TMP_PASSIVE_MODIFIER)
        battle.pending.forEach { action ->
            if (self.hasAttacked(action)) {
                self = self.addModifier(passiveModifier)
                resBattle = battle.replaceChar(self).
                addToAction(
                    BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
            }
        }
    }
    resBattle?: battle
}

val AssassinBasic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive && battle.currentTurn == 1u) {
        val statMod = if (battle.hasHighestStat(self, StatName.SPD))
            Stats(spe = 1) else Stats(def = 1)
        val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = hasUsedPassive().addModifier(passiveModifier)
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle
}

val AssassinRare: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive && battle.currentTurn == 1u) {
        val statMod = if (battle.hasHighestStat(self, StatName.HP))
            Stats(dmg = 1) else Stats(def = 1)
        val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = hasUsedPassive().addModifier(passiveModifier)
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle
}

val AssassinEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    val statMod = if (self.adjustStats().hp < self.baseStats.hp)
        Stats(dmg = 1, def = 1, spe = 1) else Stats()
    val passiveModifier = StatModifier(statMod, 0u, ModifierType.TMP_PASSIVE_MODIFIER)
    self = self.addModifier(passiveModifier)
    resBattle = battle.replaceChar(self).
    addToAction(
        BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    resBattle
}

val AlchemistBasic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(canUsePassive && battle.currentTurn == 1u){
        val statMod = if (battle.hasHighestStat(self, StatName.HP))
            Stats(dmg = 1) else Stats(def = 1)
        val passiveModifier = StatModifier(statMod, 0u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = hasUsedPassive().addModifier(passiveModifier)
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))

    }
    resBattle
}

val AlchemistRare: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive) {
        if (self.hasBeenDamaged()) {
            val statMod = if (self.adjustStats().hp + 2 > self.baseStats.hp)
                Stats(hp = 1) else Stats(hp = 2)
            val passiveModifier = StatModifier(
                statMod, 0u,
                ModifierType.PERMANENT_PASSIVE_MODIFIER
            )
            self = hasUsedPassive().addModifier(passiveModifier)
            resBattle = battle.replaceChar(self).
            addToAction(
                BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
        }
    }
    resBattle
}

val AlchemistEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive) {
        val statMod = Stats(def = 1, spe = 1, dmg = 1, hp = 1)
        val passiveModifier = StatModifier(statMod, 0u, ModifierType.PERMANENT_PASSIVE_MODIFIER)
        if (self.hasBeenDamaged()) {
            self = hasUsedPassive().addModifier(passiveModifier)
            resBattle = battle.replaceChar(self).
            addToAction(
                BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
        }
    }
    resBattle
}
val PaladinBasic: Passive<Character> = { battle ->
    var self = this as PlayableCharacter
    var modVal = 0
    (self).items.forEach { item ->
        modVal += item.stats.def
    }
    val passiveModifier = StatModifier(Stats(dmg = modVal, def = -modVal), 0u, ModifierType.SELF_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self.removeModifier(previousBuff)
    self.addModifier(passiveModifier)
}

val PaladinRare: Passive<Character> = { battle->
    var self = this
    var modVal = 0
    (self as PlayableCharacter).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(hp = modVal), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self.removeModifier(previousBuff)
    self.addModifier(passiveModifier)
}

val PaladinEpic: Passive<Character> = { battle ->
    var self = this as PlayableCharacter
    var modVal = 0
    (self).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(hp = modVal), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self.removeModifier(previousBuff)
    self.addModifier(passiveModifier)
}

val ElfBasic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive && battle.currentTurn == 1u) {
        val statMod = if (battle.hasHighestStat(self as PlayableCharacter, StatName.DMG))
            Stats(def = 1) else Stats(spe = 1)
        val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = hasUsedPassive().addModifier(passiveModifier)
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle
}

val ElfRare: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive && battle.currentTurn == 1u) {
        val statMod = Stats(spe = 1)
        val passiveModifier = StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER)
        battle.pending.forEach { actions ->
            if (self.hasAttacked(actions)) {
                self = hasUsedPassive().addModifier(passiveModifier)
                resBattle = battle.replaceChar(self).
                addToAction(
                    BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
            }
        }
    }
    resBattle?: battle
}

val ElfEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive && battle.currentTurn == 1u) {
        val statMod = Stats(def = 2)
        val passiveModifier = StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER)
        if (self.hasLostHP()) {
            self = self.addModifier(passiveModifier)
            resBattle = battle.replaceChar(self).
            addToAction(
                BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
        }
    }
    resBattle
}


val TaoistBasic:Passive<Character> = { battle->
    var self = this as PlayableCharacter
    var mod = 0
    self.items.forEach { item ->
        mod+=item.stats.spe
    }
    val passiveModifier = StatModifier(Stats(spe=-mod, dmg=mod), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
    self.addModifier(passiveModifier)
}

val TaoistRare:Passive<Character> = { battle->
    var self = this as PlayableCharacter
    if(self.items.isEmpty()){
        val passiveModifier = StatModifier(Stats(dmg=1, spe=1), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
        val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
        if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
        self.addModifier(passiveModifier)
    }
    self
}

val TaoistEpic: Passive<Character> = { battle->
    var self = this as PlayableCharacter
    if(self.items.isEmpty()){
        val passiveModifier = StatModifier(Stats(def=1,dmg=1, spe=1), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
        val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
        if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
        self.addModifier(passiveModifier)
    }
    self
}

val ThiefBasic: Passive<Battle> = { battle->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(!hasBeenDamaged() && canUsePassive){
        val statMod = Stats(spe = 1)
        self = addModifier(StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER))
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    else{
        self = hasUsedPassive()
    }
    resBattle
}

val ThiefRare: Passive<Battle> = { battle->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(!hasBeenDamaged() && canUsePassive){
        val statMod = Stats(spe = 1)
        self = addModifier(StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER))
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    else{
        self = hasUsedPassive()
        val statMod = Stats(def = 2)
        self = addModifier(StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER))
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle
}


val ThiefEpic: Passive<Battle> = { battle->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if(!hasBeenDamaged() && canUsePassive){
        val statMod = Stats(spe = 1)
        self = addModifier(StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER))
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    else{
        self = hasUsedPassive()
        val statMod = Stats(def = 2)
        self = addModifier(StatModifier(statMod, 2u, ModifierType.TMP_PASSIVE_MODIFIER))
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle.pending.forEach { action ->
        if (hasAttacked(action)) {
            val rngbs = Random.nextInt(1, 21)

            if (rngbs == 20 && (self as PlayableCharacter).items.size < ITEM_CAPACITY) {
                val target = action.target as PlayableCharacter
                val equipedItem = target.items.first()
                target.items.toMutableList()-equipedItem
                (self).equipItem(equipedItem)
                resBattle.replaceChar(self)
                resBattle.replaceChar(target)
            }
            if (rngbs >= 15) {
                val target = action.target as PlayableCharacter
                val equipedItem = target.items.first()
                target.unequip(item = equipedItem)
                resBattle.replaceChar(target)
            }
        }
    }
    resBattle
}

val WerewolfBasic: Passive<Character> = { battle->
    var self = this
    var mod = 0
    (self as PlayableCharacter).items.forEach { item ->
        mod+=item.stats.dmg
    }
    val passiveModifier = StatModifier(Stats(dmg = mod), 1u, ModifierType.TMP_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
    self.addModifier(passiveModifier)
}

val WerewolfRare: Passive<Character> = { battle->
    var self = this
    var modVal = 0
    (self as PlayableCharacter).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(dmg = modVal), 1u, ModifierType.TMP_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
    self.addModifier(passiveModifier)
}


val WerewolfEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    val target = battle.pending.filter { it.action == PossibleBattleActions.ATTACK && it.origin == self }
    if (target.isNotEmpty()) {
        val mod = target.first().target?.baseStats?.def
            ?: throw BattleError.InvalidAction("Attack has no target")
        val statMod = Stats(def = -mod)
        val passiveModifier = StatModifier(statMod, 0u, ModifierType.TMP_PASSIVE_MODIFIER)
        val updatedTarget = target.first().target!!.addModifier(passiveModifier)
        resBattle = battle.replaceChar(updatedTarget).
        addToAction(
            BattleAction(self, updatedTarget, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle
}

val BerserkerBasic: Passive<Character> = { battle ->
    var self = this
    var modVal = 0
    (self as PlayableCharacter).items.forEach { item ->
        modVal += item.stats.dmg
    }
    val passiveModifier = StatModifier(Stats(dmg = -modVal, def = modVal), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
    self.addModifier(passiveModifier)}

val BerserkerRare: Passive<Character> = { battle ->
    var self = this
    var modVal = 0
    (self as PlayableCharacter).items.forEach { _ ->
        modVal++
    }
    val passiveModifier = StatModifier(Stats(def = modVal), 1u, ModifierType.SELF_PASSIVE_MODIFIER)
    val previousBuff = self.activeStatModifiers.find { it.type == ModifierType.SELF_PASSIVE_MODIFIER }
    if(previousBuff != null) self = self.removeModifier(previousBuff) as PlayableCharacter
    self.addModifier(passiveModifier)
}
val BerserkerEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    battle.pending.forEach { action ->
        if (this.hasAttacked(action)) {
            val statMod = Stats(dmg = -action.stats.hp % 2)
            val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
            val updatedTarget = action.target?.addModifier(passiveModifier)
                ?: throw BattleError.InvalidAction("No target in attack")
            resBattle = resBattle!!.replaceChar(updatedTarget).
            addToAction(
                BattleAction(self, updatedTarget, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
        }
    }
    resBattle?: battle
}

val PriestRare: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    if (this.canUsePassive) {
        self = hasUsedPassive()
        val statMod = Stats(1, 1, 1, 1)
        val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
        self = self.addModifier(passiveModifier)
        resBattle = battle.replaceChar(self).
        addToAction(
            BattleAction(self, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    }
    resBattle
}

val PriestEpic: Passive<Battle> = { battle ->
    var self = battle?.findSelf(this.name)?:throw BattleError.PassiveCantActivateOutsideBattle()
    var resBattle = battle
    val target = battle.pending.filter { it.action == PossibleBattleActions.ATTACK && it.target == self }.random().origin
    val mod = 1 - target.adjustStats().dmg
    val statMod = Stats(dmg = -mod)
    val passiveModifier = StatModifier(statMod, 1u, ModifierType.TMP_PASSIVE_MODIFIER)
    val newTarget = target.addModifier(passiveModifier)
    resBattle = battle.replaceChar(newTarget).
    addToAction(
        BattleAction(newTarget, null, PossibleBattleActions.PASSIVE, statMod, battle.currentTurn.toInt()))
    resBattle
}

val NoPassive: Passive<Character> = { battle->
    this
}



private fun Battle.findSelf(name: String) =
    this.characters.find { it.name == name }?: throw GameError.NoActiveCharacters()



