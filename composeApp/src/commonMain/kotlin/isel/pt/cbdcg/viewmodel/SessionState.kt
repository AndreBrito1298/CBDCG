package isel.pt.cbdcg.viewmodel

import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.views.startMenu.tutorial.TutorialOptions

sealed interface SessionState {
    data object SignedOut : SessionState

    data class InTutorial(
        val option: TutorialOptions
    ) : SessionState
    data class InLobby(
        val user: User,
        val tables: List<Table> = emptyList(),
    ) : SessionState
    data class InTable(
        val user: User,
        val table: Table,
    ) : SessionState
    data class InGame(
        val user: User,
        val game: Game,
    ) : SessionState
}