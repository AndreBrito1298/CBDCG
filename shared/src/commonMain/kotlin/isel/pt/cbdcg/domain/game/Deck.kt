package isel.pt.cbdcg.domain.game

typealias Deck<T> = Map<T, UInt>

fun <T> Deck<T>.draw(): T {
    val cards = this.flatMap{ (card, copies) -> List(copies.toInt()){ card } }
    return cards.random()
}
fun <T> Deck<T>.remove(removedCard: T): Deck<T> =
    this.map{ (card, copies) -> if(card == removedCard) card to copies - 1u else card to copies }
        .toMap()

