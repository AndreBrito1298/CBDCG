package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.CharacterDTO

data class PlayableCharacter(
    override val name: String,
    override val baseStats: Stats,
    override val activeStatModifiers: List<StatModifier> = listOf(),
    override val grade: Grade,
    val items: List<Item> = listOf()
) : Character {

    override val type: CharacterType = CharacterType.PLAYABLE

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
            item = items.map{ it.toItemDTO() }.toTypedArray()
        )
}

fun CharacterDTO.toPlayableCharacter(): PlayableCharacter =
    PlayableCharacter(
        name = this.name,
        baseStats = this.baseStats.toStats(),
        activeStatModifiers = this.activeModifiers.map{ it.toModifier() },
        grade = grade.toGrade()
    )
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
        PlayableCharacter(name = "scrap_robot", baseStats = Stats(1, 2, 3, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "juggernaut", baseStats = Stats(3, 3, 1, 2), grade = Grade.BASIC),
        PlayableCharacter(name = "necromancer", baseStats = Stats(3, 2, 1, 3), grade = Grade.BASIC),
        PlayableCharacter(name = "taoist", baseStats = Stats(2, 2, 2, 3), grade = Grade.BASIC),
    )
}