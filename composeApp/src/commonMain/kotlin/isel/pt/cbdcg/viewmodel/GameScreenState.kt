package isel.pt.cbdcg.viewmodel

import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.Player

data class GameUI(val state: GameUIState, val boardZoom: Float = 1.0f)
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
}