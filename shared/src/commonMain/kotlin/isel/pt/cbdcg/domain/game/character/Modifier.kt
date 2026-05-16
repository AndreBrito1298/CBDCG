package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.board.BoardTile

class Modifier(
    val stat: Stats,
    val positive: Boolean,
    val duration: UInt,
) {

}