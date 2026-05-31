package isel.pt.cbdcg.viewmodel

import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.BoardTile

data class GameUI(
    val state: GameUIState,
    val boardZoom: Float = 1.0f,
    val movementUsed: Int = 0
)
sealed interface GameUIState {

    data object Idle : GameUIState

    data class SelectCard(
        val idx: UInt,
        val card: Card
    ) : GameUIState

    data class PlacingCard(
        val idx: UInt,
        val card: Card
    ) : GameUIState

    data class InspectCard(
        val card: Card,
        val previous: GameUIState = Idle
    ) : GameUIState

    data class InspectPlayer(
        val player: Player,
    ) : GameUIState

    data class SelectBoardCharacter(
        val position: BoardTile,
    ) : GameUIState

    data class MovingCharacter(
        val from: BoardTile,
        val path: List<BoardTile> = emptyList()
    ) : GameUIState

    data class InspectTileEffect(
        val boardTile: BoardTile
    ) : GameUIState

    data class GameOver(
        val winner: Player
    ) : GameUIState
}