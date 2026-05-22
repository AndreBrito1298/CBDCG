package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.CharacterDTO

data class PlayableCharacter(
    override val name: String,
    override val baseStats: Stats,
    override val activeModifiers: List<Modifier> = listOf(),
    override val grade: Grade,
    val items: List<Item> = listOf()
) : Character {

    override val type: CharacterType = CharacterType.PLAYABLE

    override fun addModifier(newModifier: Modifier): Character {
        return copy(activeModifiers = activeModifiers.plus(newModifier))
    }
    override fun removeModifier(modifier: Modifier): Character {
        return copy(activeModifiers = activeModifiers.minus(modifier))
    }

    override fun toCharacterDTO(): CharacterDTO =
        CharacterDTO(
            type = "P",
            name = name,
            baseStats = baseStats.toString(),
            activeModifiers = activeModifiers.map{ it.toModifierDTO() }.toTypedArray(),
            grade = grade.code(),
            item = items.map{ it.toItemDTO() }.toTypedArray()
        )
}

fun CharacterDTO.toPlayableCharacter(): PlayableCharacter =
    PlayableCharacter(
        name = this.name,
        baseStats = this.baseStats.toStats(),
        activeModifiers = this.activeModifiers.map{ it.toModifier() },
        grade = grade.toGrade()
    )
object PlayableCharacterCatalog {
    val playableCharacters = listOf(
        PlayableCharacter(name = "trainee", baseStats = Stats(3u, 2u, 2u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "thief", baseStats = Stats(2u, 2u, 2u, 3u), grade = Grade.BASIC),
        PlayableCharacter(name = "apprentice", baseStats = Stats(2u, 3u, 2u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "ninja", baseStats = Stats(2u, 4u, 1u, 3u), grade = Grade.BASIC),
        PlayableCharacter(name = "alchemist", baseStats = Stats(3u, 3u, 1u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "strange_alien", baseStats = Stats(3u, 2u, 2u, 1u), grade = Grade.BASIC),
        PlayableCharacter(name = "vampire", baseStats = Stats(2u, 3u, 2u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "guardian", baseStats = Stats(2u, 4u, 1u, 3u), grade = Grade.BASIC),
        PlayableCharacter(name = "elf", baseStats = Stats(2u, 2u, 2u, 3u), grade = Grade.BASIC),
        PlayableCharacter(name = "beast_warrior", baseStats = Stats(2u, 3u, 2u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "nun", baseStats = Stats(2u, 4u, 1u, 3u), grade = Grade.BASIC),
        PlayableCharacter(name = "druid", baseStats = Stats(3u, 3u, 1u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "scrap_robot", baseStats = Stats(3u, 2u, 2u, 1u), grade = Grade.BASIC),
        PlayableCharacter(name = "juggernaut", baseStats = Stats(2u, 3u, 2u, 2u), grade = Grade.BASIC),
        PlayableCharacter(name = "necromancer", baseStats = Stats(2u, 4u, 1u, 3u), grade = Grade.BASIC),
        PlayableCharacter(name = "taoist", baseStats = Stats(2u, 4u, 1u, 3u), grade = Grade.BASIC),
    )
}