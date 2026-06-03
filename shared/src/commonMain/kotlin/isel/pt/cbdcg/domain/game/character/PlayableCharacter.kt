package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.board.replaceBoardTile
import isel.pt.cbdcg.dto.CharacterDTO
import isel.pt.cbdcg.error.CharacterError

data class PlayableCharacter(
    override val name: String,
    override val baseStats: Stats,
    override val activeStatModifiers: List<StatModifier> = listOf(),
    override val grade: Grade,
    val items: List<Item> = listOf(),
    val maxItems: Int = 1
) : Character {

    override val role: CharacterRole = CharacterRole.PLAYABLE

    override fun addModifier(newStatModifier: StatModifier): Character {
        return copy(activeStatModifiers = activeStatModifiers.plus(newStatModifier))
    }
    override fun removeModifier(statModifier: StatModifier): Character {
        return copy(activeStatModifiers = activeStatModifiers.minus(statModifier))
    }

    override fun toCharacterDTO(): CharacterDTO =
        CharacterDTO(
            type = "P",
            name = name,
            baseStats = baseStats.toString(),
            activeModifiers = activeStatModifiers.map{ it.toModifierDTO() }.toTypedArray(),
            grade = grade.code(),
            items = items.map{ it.toItemDTO() }.toTypedArray()
        )

    override fun applyToGame(game: Game): Game {
        game.board.tiles.forEach { tile ->
            if(tile.character != null){
                if(tile.character.name == this.name){
                    val tile = tile.copy(character =  this)
                    return game.copy(board = game.board.replaceBoardTile(tile))
                }
            }
        }
        return game
    }
}

fun PlayableCharacter.equipItem(item: Item): PlayableCharacter {

    if(items.size >= maxItems)
        throw CharacterError.ItemCapacityLimit(maxItems)

    return copy(items = items + item)
}
fun PlayableCharacter.unequip(item: Item): PlayableCharacter = copy(items = items - item)

fun CharacterDTO.toPlayableCharacter(): PlayableCharacter =
    PlayableCharacter(
        name = name,
        baseStats = baseStats.toStats(),
        activeStatModifiers = activeModifiers.map{ it.toModifier() },
        items = items.map{ it.toItem() },
        grade = grade.toGrade()
    )

fun getPlayableCharacterByName(name: String): PlayableCharacter? = PlayableCharacterCatalog.playableCharacters.find { it.name == name }
object PlayableCharacterCatalog {
    val playableCharacters = listOf(
        PlayableCharacter(name = "trainee", baseStats = Stats(3, 2, 2, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "thief", baseStats = Stats(2, 2, 2, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "apprentice", baseStats = Stats(2, 3, 2, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "ninja", baseStats = Stats(2, 4, 1, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "alchemist", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "strange_alien", baseStats = Stats(3, 2, 2, 1), grade = Grade.BASIC),
        PlayableCharacter(name = "vampire", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "guardian", baseStats = Stats(3, 2, 3, 1), grade = Grade.BASIC),
        PlayableCharacter(name = "elf", baseStats = Stats(3, 2, 1, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "beast_warrior", baseStats = Stats(4, 1, 1, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "nun", baseStats = Stats(3, 1, 1, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "druid", baseStats = Stats(2, 3, 2, 1), grade = Grade.BASIC),
        PlayableCharacter(name = "scrap_robot", baseStats = Stats(1, 2, 3, 3), grade = Grade.BASIC, maxItems = 2),
        PlayableCharacter(name = "juggernaut", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "necromancer", baseStats = Stats(3, 2, 1, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "taoist", baseStats = Stats(2, 2, 2, 3), grade = Grade.BASIC),
    )
}