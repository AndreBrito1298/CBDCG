package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.character.KnightBasic.activatePassive
import isel.pt.cbdcg.domain.game.character.KnightBasic.canActivate


enum class passiveType {
    ALWAYS_ACTIVE,
    ONE_TIME_USE,
}

interface Passive{
    val type: passiveType

    var wasActivated : Boolean

    val statChange: StatModifier

    fun PlayableCharacter.canActivate(battle: Battle): Boolean

    fun PlayableCharacter.activatePassive(): PlayableCharacter
}

object KnightBasic: Passive {
    override val type: passiveType
        get() = passiveType.ONE_TIME_USE
    override var wasActivated: Boolean
        get() = false
        set(value) {}
    override val statChange: StatModifier
        get() = StatModifier(Stats(def = 1), duration = 2u, type = ModifierType.PASSIVE_MODIFIER)

    override fun PlayableCharacter.canActivate(battle: Battle): Boolean {
        val resChar = battle.characters.find { it.name == name && it.grade == grade}!! as PlayableCharacter
        TODO()
    }

    override fun PlayableCharacter.activatePassive(): PlayableCharacter {
        wasActivated = true
     //   return this.copy(activeStatModifiers = this.addModifier(statChange))
        TODO()
    }
}


object ThiefBasic: Passive {
    override val type: passiveType
        get() = passiveType.ALWAYS_ACTIVE

    override var wasActivated: Boolean
        get() = false
        set(value) {}

    override val statChange: StatModifier
        get() = StatModifier(Stats(), duration = 1u, type = ModifierType.PASSIVE_MODIFIER) //evade chance

    override fun PlayableCharacter.canActivate(battle: Battle) = true

    override fun PlayableCharacter.activatePassive(): PlayableCharacter {
        var spe = this.baseStats.spe
        this.activeStatModifiers.forEach { spe += it.stats.spe }
        return this.copy(activeStatModifiers = this.activeStatModifiers.toMutableList()+statChange)//*spe
    }
}

object MageBasic: Passive {
    override val type: passiveType
        get() = TODO("Not yet implemented")
    override var wasActivated: Boolean
        get() = TODO("Not yet implemented")
        set(value) {}
    override val statChange: StatModifier
        get() = TODO("Not yet implemented")

    override fun PlayableCharacter.canActivate(battle: Battle): Boolean {
        TODO("Not yet implemented")
    }

    override fun PlayableCharacter.activatePassive(): PlayableCharacter {
        TODO("Not yet implemented")
    }

}



fun Battle.resolvePassive(passive: Passive): Battle {
    var resBattle = this
    this.characters.forEach { character ->
        val pc = character as PlayableCharacter
        if(pc.canActivate(this)){
            pc.activatePassive()
            characters.toMutableList().remove(pc)
            resBattle = resBattle.copy(characters = characters+pc.activatePassive())
        }
    }
    return resBattle
}