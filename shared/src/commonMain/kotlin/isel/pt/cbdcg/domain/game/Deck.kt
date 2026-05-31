package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.AllTileEffects
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.TileEffectTypes
import isel.pt.cbdcg.error.GameError

typealias Deck<T> = Map<T, UInt>

fun Deck<Tile>.applyRandomSpecialEffects(): Deck<Tile> {
    val tiles = this.flatMap{ (tile, copies) -> List(copies.toInt()){ tile } }.toMutableList()

    val effectsList = AllTileEffects.allTileEffects
        .flatMap{ (effect, copies) -> List(copies.toInt()) { effect } }
        .shuffled()

    val randomTiles = tiles.indices
        .filter{ idx -> tiles[idx].specialEffect.type == TileEffectTypes.None }
        .shuffled()
        .take(effectsList.size)

    randomTiles
        .zip(effectsList)
        .forEach{ (idx, effect) -> tiles[idx] = tiles[idx].copy(specialEffect = effect) }

    return tiles.groupingBy { it }.eachCount().mapValues { (_, copies) -> copies.toUInt() }

}
fun <T> Deck<T>.draw(): T {
    val cards = this.flatMap{ (card, copies) -> List(copies.toInt()){ card } }
    if(cards.isEmpty()) throw GameError.EmptyDeck()
    return cards.random()
}
fun <T> Deck<T>.remove(removedCard: T): Deck<T> =
    this.map{ (card, copies) -> if(card == removedCard) card to copies - 1u else card to copies }
        .toMap()

