package isel.pt.cbdcg.domain.game.character

interface Character{
    val name: String
    val stats: Stats
    val string: String
}

fun String.toCharacter(): Character? =
    when(this[0]){
        'P' -> this.decodeCharacter()
        else -> null
    }