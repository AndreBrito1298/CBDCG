package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.board.Entity
import isel.pt.cbdcg.dto.CharacterDTO
import isel.pt.cbdcg.error.GameError

enum class CharacterRole{ PLAYABLE, ENEMY }

fun String.toCharacterType(): CharacterRole =
    when(this[0]){
        'P' -> CharacterRole.PLAYABLE
        'E' -> CharacterRole.ENEMY
        else -> throw GameError.InvalidFormat("Character", this)
    }

interface Character: Entity {
    val role: CharacterRole
    val name: String
    val baseStats: Stats
    val activeStatModifiers: List<StatModifier>
    val grade: Grade

    fun addModifier(newStatModifier: StatModifier): Character
    fun removeModifier(statModifier: StatModifier): Character
    fun toCharacterDTO(): CharacterDTO
}

fun CharacterDTO.toCharacter(): Character =
    when(this.type.toCharacterType()){
        CharacterRole.PLAYABLE -> this.toPlayableCharacter()
        CharacterRole.ENEMY -> TODO()
    }