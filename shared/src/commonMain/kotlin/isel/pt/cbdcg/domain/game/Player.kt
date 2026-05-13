package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.dto.PlayerDTO
import isel.pt.cbdcg.dto.toUserDTO
import kotlin.collections.plus

typealias PlayerHand = Map<UInt, Card>

fun PlayerHand.numTileCards(): Int =
    this.values.filter{ it.type == CardType.TILE }.size

data class Player(
    val user: User,
    val hand: PlayerHand,
    val currentCharacter: String? = null
) {

    fun addToHand(card: Card): Player {
        val lastKey = this.hand.keys.lastOrNull() ?: 0u
        val updatedHand = this.hand.plus(lastKey to card)
        return copy(hand = updatedHand)
    }
    fun removeFromHand(idx: UInt): Player {
        val updatedHand = hand
            .filterKeys{ it != idx }.values
            .mapIndexed{ newIdx, card -> newIdx.toUInt() to card }
            .toMap()
        return copy(hand = updatedHand)
    }

    fun toPlayerDTO(): PlayerDTO =
        PlayerDTO(
            user = user.toUserDTO(),
            hand = hand.map{ (idx, card) -> "$idx|${card.string}" }.toTypedArray()
        )
}