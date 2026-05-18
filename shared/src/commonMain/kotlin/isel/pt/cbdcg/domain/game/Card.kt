package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.toTile
import isel.pt.cbdcg.domain.game.board.toTileDTO
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.toPlayableCharacter
import isel.pt.cbdcg.dto.CardDTO
import isel.pt.cbdcg.error.GameError


enum class CardType {
    TILE, CHARACTER
}

fun String.toCardType(): CardType =
    when(this[0]){
        'T' -> CardType.TILE
        'C' -> CardType.CHARACTER
        else -> throw GameError.InvalidCardType(this)
    }

sealed interface Card {
    val type: CardType

    fun toCardDTO(): CardDTO
}

data class TileCard(
    val tile: Tile
) : Card {
    override val type = CardType.TILE
    override fun toCardDTO(): CardDTO =
        CardDTO(
            type = "T",
            tile = tile.toTileDTO(),
            character = null
        )
}

data class CharacterCard(
    val character: PlayableCharacter
) : Card {
    override val type = CardType.CHARACTER
    override fun toCardDTO(): CardDTO =
        CardDTO(
            type = "C",
            tile = null,
            character = character.toCharacterDTO()
        )
}

fun CardDTO.toCard(): Card =
    when(type.toCardType()) {
        CardType.TILE ->
            TileCard(tile?.toTile() ?: throw GameError.InvalidCardFormat("TileCard does not contain a Tile"))
        CardType.CHARACTER ->
            CharacterCard(character?.toPlayableCharacter() ?: throw GameError.InvalidCardFormat("CharacterCard does not contain a Character"))
    }

