package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.decodeTile
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.decodeCharacter


enum class CardType {
    TILE, CHARACTER
}
sealed interface Card {
    val type: CardType
    val string: String
}

fun String.decodeCard(type: Char): Card? =
    when(type) {
        'T' -> TileCard(this.decodeTile())
        'C' -> CharacterCard(this.decodeCharacter())
        else -> null
    }

data class TileCard(
    val tile: Tile
) : Card {
    override val type = CardType.TILE
    override val string = "T|${tile}"
}

data class CharacterCard(
    val character: PlayableCharacter
) : Card {
    override val type = CardType.CHARACTER
    override val string = "C|${character}"
}
