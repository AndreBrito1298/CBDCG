package isel.pt.cbdcg.views

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import isel.pt.cbdcg.ClientApi
import isel.pt.cbdcg.domain.AuthUser
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.views.lobby.SearchTablesScreen
import isel.pt.cbdcg.views.lobby.WaitingTableScreen
import isel.pt.cbdcg.views.startMenu.CreateUserScreen
import isel.pt.cbdcg.views.startMenu.IdleMenuScreen
import isel.pt.cbdcg.views.startMenu.LoginScreen

sealed interface PossibleStates {
    data object Idle: PossibleStates
    data object Login: PossibleStates
    data object Create: PossibleStates
    data class SearchTables(val auth: AuthUser): PossibleStates
    data class WaitingTable(val auth: AuthUser, val participant: Participant): PossibleStates
}

@Composable
fun ScreenState(clientApi: ClientApi) {

    var mode by rememberSaveable { mutableStateOf<PossibleStates>(PossibleStates.Idle) }

    when(val current = mode){

        is PossibleStates.Idle -> {
            IdleMenuScreen { newMode: PossibleStates ->
                mode = newMode
            }
        }

        is PossibleStates.Login -> {
            LoginScreen(
                clientApi,
                { mode = PossibleStates.Idle },
                { auth -> mode = PossibleStates.SearchTables(auth) }
            )
        }

        is PossibleStates.Create -> {
            CreateUserScreen(
                clientApi,
                { mode = PossibleStates.Idle },
                { auth -> mode = PossibleStates.SearchTables(auth) }
            )
        }

        is PossibleStates.SearchTables -> {
            SearchTablesScreen(
                clientApi,
                current.auth,
                { mode = PossibleStates.Idle },
                { table -> mode = PossibleStates.WaitingTable(current.auth, table) }
            )
        }

        is PossibleStates.WaitingTable -> {
            WaitingTableScreen(
                clientApi,
                current.auth,
                current.participant
            ) { mode = PossibleStates.SearchTables(current.auth) }
        }

    }
}
