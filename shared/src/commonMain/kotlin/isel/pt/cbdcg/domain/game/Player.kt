package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.dto.PlayerDTO
import kotlin.collections.plus

data class Player(
    val user: UInt,
    val hand: Map<UInt, Tile>
) {

    fun addToHand(card: Tile): Player {
        val lastKey = this.hand.keys.lastOrNull() ?: 0u
        val updatedHand = this.hand.plus(lastKey to card)
        return copy(hand = updatedHand)
    }
    fun removeFromHand(idx: UInt): Player {
        val updatedHand = hand
            .filterKeys{ it != idx }.values
            .mapIndexed{ newIdx, tile -> newIdx.toUInt() to tile }
            .toMap()
        return copy(hand = updatedHand)
    }

    fun toPlayerInfo(): PlayerDTO =
        PlayerDTO(
            user = user.toInt(),
            hand = hand.map{ (idx, tile) -> "$idx|${tile.codeString()}" }.toTypedArray()
        )
}