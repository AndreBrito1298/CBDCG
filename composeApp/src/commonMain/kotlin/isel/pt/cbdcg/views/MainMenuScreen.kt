package isel.pt.cbdcg.views

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import isel.pt.cbdcg.ClientApi
import isel.pt.cbdcg.domain.User

enum class MainMenuStates {
    Idle, Login, Create, SearchTables, InTable
}

@Composable
fun MainMenuScreen(clientApi: ClientApi) {

    var mode by rememberSaveable { mutableStateOf(MainMenuStates.Idle) }
    var authUser by rememberSaveable { mutableStateOf<User?>(null) }

    when(mode){

        MainMenuStates.Idle -> {
            IdleMenuScreen{ newMode: MainMenuStates -> mode = newMode }
        }

        MainMenuStates.Login -> {
            LoginScreen(
                clientApi,
                { mode = MainMenuStates.Idle },
                { authUser = it }
            )
        }

        MainMenuStates.Create -> {
            CreateUserScreen(clientApi,
                { mode = MainMenuStates.Idle },
                { authUser = it }
            )
        }

        MainMenuStates.SearchTables -> {
            if(authUser != null) {
                SearchTablesScreen(
                    clientApi,
                    authUser!!,
                    { mode = MainMenuStates.Idle }
                )
            }
            else
                LoginScreen(
                    clientApi,
                    { mode = MainMenuStates.Idle },
                    { authUser = it }
                )
        }

        MainMenuStates.InTable -> {
            TODO()
        }

    }
}