package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.board.Entity
import isel.pt.cbdcg.dto.CharacterDTO
import isel.pt.cbdcg.error.GameError

enum class CharacterType{ PLAYABLE, ENEMY }

fun String.toCharacterType(): CharacterType =
    when(this[0]){
        'P' -> CharacterType.PLAYABLE
        'E' -> CharacterType.ENEMY
        else -> throw GameError.InvalidCharacterType(this)
    }

interface Character: Entity {
    val type: CharacterType
    val name: String
    val baseStats: Stats
    val activeModifiers: List<Modifier>


    fun addModifier(newModifier: Modifier): Character
    fun removeModifier(modifier: Modifier): Character
    fun toCharacterDTO(): CharacterDTO
}

fun CharacterDTO.toCharacter(): Character =
    when(this.type.toCharacterType()){
        CharacterType.PLAYABLE -> this.toPlayableCharacter()
        CharacterType.ENEMY -> TODO()
    }