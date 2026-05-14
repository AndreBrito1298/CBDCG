package isel.pt.cbdcg.domain.game.character

data class PlayableCharacter(
    override val name: String,
    override val stats: Stats,
) : Character {

    override val string = "P#${name}#${stats}"
}

fun String.decodeCharacter(): PlayableCharacter {
    val (_, name, stats) = this.split("#")
    val (hp, atk, def, spe) = stats.split("&")
    return PlayableCharacter(
        name = name,
        stats = Stats(hp.toUInt(), atk.toUInt(), def.toUInt(), spe.toUInt())
    )
}

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