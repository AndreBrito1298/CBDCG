package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.dto.BattleDTO
import kotlin.random.Random

data class Battle(
    val characters: List<Character>,
    val turn: UInt,
    val currentTurn: Character
)

fun Battle.toBattleDTO(): BattleDTO =
    BattleDTO(
        characters = characters.map{ it.toCharacterDTO() }.toTypedArray(),
        turn = turn.toInt(),
        currentTurn = currentTurn.toCharacterDTO()
    )
fun BattleDTO.toBattle(): Battle =
    Battle(
        characters = characters.map{ it.toCharacter() },
        turn = turn.toUInt(),
        currentTurn = currentTurn.toCharacter()
    )

fun Battle.attack(target: Character): Battle {

    val dodge = target.adjustStats().spe - currentTurn.adjustStats().spe
    val dodgeChance = dodge / 33
    if(Random.nextFloat() <= dodgeChance) return this

    val damage = currentTurn.adjustStats().dmg - target.adjustStats().def

    val stats = Stats(
        hp = if(damage > 0) -damage else 0,
        dmg = 0,
        def = if(damage > 0) -1 else damage,
        spe = 0
    )

    val characters = characters.map { character ->
        if (character == target)
            character.addModifier(
                StatModifier(
                    stats = stats,
                    duration = -1,
                    type = ModifierType.BATTLE
                )
            )
        else character
    }

    return copy(characters = characters)
}