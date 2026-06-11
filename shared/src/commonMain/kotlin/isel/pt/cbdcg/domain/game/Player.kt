package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.toUserDTO
import isel.pt.cbdcg.dto.PlayerDTO
import isel.pt.cbdcg.dto.toUser
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.plus

typealias PlayerHand = Map<UInt, Card>

fun PlayerHand.numTileCards(): Int =
    this.values.filter{ it.type == CardType.TILE }.size

fun PlayerHand.containsAllKeys(): Boolean =
    this.values.filter { card -> card is ItemCard && card.item.grade == Grade.KEY }.size == 5

data class Player(
    val user: User,
    val hand: PlayerHand,
    val currentCharacter: String?,
) : Entity {
    override fun applyToGame(game: Game): Game {
        val newPlayerList = game.players.filter { it.user != this.user }.toMutableList()
        newPlayerList.add(this)
        return game.copy(players = newPlayerList)
    }
}


fun Player.addToHand(card: Card): Player {
    val lastKey = this.hand.keys.lastOrNull() ?: 0u
    val updatedHand = this.hand.plus(lastKey + 1u to card)
    return copy(hand = updatedHand)
}
fun Player.removeFromHand(idx: UInt): Player {

    val card = hand.get(idx)
    val updatedCharacter =
        if(card != null && card.type == CardType.CHARACTER) (card as CharacterCard).character.name
        else this.currentCharacter

    val updatedHand = hand
        .filterKeys{ it != idx }.values
        .mapIndexed{ newIdx, card -> newIdx.toUInt() to card }
        .toMap()

    return copy(hand = updatedHand, currentCharacter = updatedCharacter)
}

fun Player.toPlayerDTO(): PlayerDTO =
    PlayerDTO(
        user = user.toUserDTO(),
        hand = hand.map{ (_, card) -> card.toCardDTO() }.toTypedArray(),
        currentCharacter = currentCharacter
    )


fun PlayerDTO.toPlayer(): Player{

    val hand = hand.mapIndexed { index, card -> index.toUInt() to card.toCard() }.toMap()

    return Player(
        user = user.toUser(),
        hand = hand,
        currentCharacter = currentCharacter
    )

}