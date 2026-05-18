package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.CharacterDTO

data class PlayableCharacter(
    override val name: String,
    override val baseStats: Stats,
    override val activeModifiers: List<Modifier> = listOf(),
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
            activeModifiers = activeModifiers.map{ it.toModifierDTO() }.toTypedArray()
        )
}

fun CharacterDTO.toPlayableCharacter(): PlayableCharacter =
    PlayableCharacter(
        name = this.name,
        baseStats = this.baseStats.toStats(),
        activeModifiers = this.activeModifiers.map{ it.toModifier() }

    )
object PlayableCharacterCatalog {
    val playableCharacters = listOf(
        PlayableCharacter("trainee", Stats(3u, 2u, 2u, 2u)),
        PlayableCharacter("thief", Stats(2u, 2u, 2u, 3u)),
        PlayableCharacter("apprentice", Stats(2u, 3u, 2u, 2u)),
        PlayableCharacter("ninja", Stats(2u, 4u, 1u, 3u)),
        PlayableCharacter("alchemist", Stats(3u, 3u, 1u, 2u)),
        PlayableCharacter("strange_alien", Stats(3u, 2u, 2u, 1u)),
        PlayableCharacter("vampire", Stats(2u, 3u, 2u, 2u)),
        PlayableCharacter("guardian", Stats(2u, 4u, 1u, 3u)),
        PlayableCharacter("elf", Stats(2u, 2u, 2u, 3u)),
        PlayableCharacter("beast_warrior", Stats(2u, 3u, 2u, 2u)),
        PlayableCharacter("nun", Stats(2u, 4u, 1u, 3u)),
        PlayableCharacter("druid", Stats(3u, 3u, 1u, 2u)),
        PlayableCharacter("scrap_robot", Stats(3u, 2u, 2u, 1u)),
        PlayableCharacter("juggernaut", Stats(2u, 3u, 2u, 2u)),
        PlayableCharacter("necromancer", Stats(2u, 4u, 1u, 3u)),
        PlayableCharacter("taoist", Stats(2u, 4u, 1u, 3u)),
    )
}