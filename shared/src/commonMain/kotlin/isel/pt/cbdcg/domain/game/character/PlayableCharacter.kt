package isel.pt.cbdcg.domain.game.character

data class PlayableCharacter(
    override val name: String,
    override val stats: Stats,
    val activeModifiers: List<Modifier> = listOf(),
) : Character {

    override fun toString() = "${name}#${stats}"

    override fun editStats(newStats: Stats): Character {
        return this.copy(stats = newStats)
    }
    val string = "P#${name}#${stats}"

    //Lista de modifiers apenas irá manter os ativos, quando o objeto é composto turn é comparada a currTurn e apenas é adicionado ao ativo se esta for menor
    //Ou seja no objeto presistido apenas são colocados os modifiers que estarão ativos na prox turn
    //Como em BD players estarão separados dá facilmente para comparar os objetos e aplicar o filtro a todos os modificadores ativos, se facilitar isto pode ser movido
    //VER se faz sentido mover deve tar no local de mais facil acesso para limpeza dos modificadores que já nao estao ativos
    fun addModifier(modifier: Modifier):PlayableCharacter {
        return this.copy(activeModifiers = activeModifiers.toMutableList().apply { add(modifier) })
    }
}

fun String.decodeCharacter(): PlayableCharacter {
    val (_, name, stats) = this.split("#")
    val (hp, atk, def, spe) = stats.split("&")
    return PlayableCharacter(
        name = name,
        stats = Stats(hp.toUInt(), atk.toUInt(), def.toUInt(), spe.toUInt()),
        activeModifiers = listOf(),
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