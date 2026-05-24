package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.toTile
import isel.pt.cbdcg.domain.game.board.toTileDTO
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.toItem
import isel.pt.cbdcg.domain.game.character.toItemDTO
import isel.pt.cbdcg.domain.game.character.toPlayableCharacter
import isel.pt.cbdcg.dto.CardDTO
import isel.pt.cbdcg.error.GameError


enum class CardType {
    TILE, CHARACTER, ITEM
}

fun String.toCardType(): CardType =
    when(this[0]){
        'T' -> CardType.TILE
        'C' -> CardType.CHARACTER
        'I' -> CardType.ITEM
        else -> throw GameError.InvalidFormat("Card Type", this)
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
            character = null,
            item = null
        )
}

data class CharacterCard(
    val character: Character
) : Card {
    override val type = CardType.CHARACTER
    override fun toCardDTO(): CardDTO =
        CardDTO(
            type = "C",
            tile = null,
            character = character.toCharacterDTO(),
            item = null
        )
}

data class ItemCard(
    val item: Item
) : Card {
    override val type = CardType.ITEM
    override fun toCardDTO(): CardDTO =
        CardDTO(
            type = "I",
            tile = null,
            character = null,
            item = item.toItemDTO()
        )
}

fun CardDTO.toCard(): Card =
    when(type.toCardType()) {
        CardType.TILE ->
            TileCard(tile?.toTile() ?: throw GameError.InvalidCardFormat("TileCard does not contain a Tile"))
        CardType.CHARACTER ->
            CharacterCard(character?.toPlayableCharacter() ?: throw GameError.InvalidCardFormat("CharacterCard does not contain a Character"))
        CardType.ITEM ->
            ItemCard(item = item?.toItem() ?: throw GameError.InvalidCardFormat("ItemCard does not contain an Item"))
    }

