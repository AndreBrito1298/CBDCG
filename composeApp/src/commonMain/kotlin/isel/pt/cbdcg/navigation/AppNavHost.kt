package isel.pt.cbdcg.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import isel.pt.cbdcg.AppViewModel
import isel.pt.cbdcg.views.lobby.SearchTablesScreen
import isel.pt.cbdcg.views.lobby.WaitingTableScreen
import isel.pt.cbdcg.views.startMenu.CreateUserScreen
import isel.pt.cbdcg.views.startMenu.IdleMenuScreen
import isel.pt.cbdcg.views.startMenu.LoginScreen
import isel.pt.cbdcg.views.utils.DisplayError


@Composable
fun AppNavHost(vm: AppViewModel) {

    val nav = rememberNavController()
    val ui by vm.ui.collectAsState()

    NavHost(
        navController = nav,
        startDestination = IdleRoute
    ){

        composable<IdleRoute> {
            IdleMenuScreen(
                user = ui.user,
                loginNav = { nav.navigate(LoginRoute) },
                createUserNav = { nav.navigate(CreateUserRoute) },
                searchTablesNav = { nav.navigate(SearchTablesRoute) },
                logout = { vm.logout(onSuccess = { nav.popBackStack(IdleRoute, inclusive = false) }) },
            )
        }

        composable<LoginRoute> {
            LoginScreen(
                mainMenuNav = { vm.stopObserving({ nav.navigateUp() }) },
                login = { email, password ->
                    vm.login(
                        email = email,
                        password = password,
                        onSuccess = { nav.navigate(SearchTablesRoute){ popUpTo(IdleRoute) { inclusive = false } } }
                    )
                }
            )
        }

        composable<CreateUserRoute> {
            CreateUserScreen(
                mainMenuNav = { vm.stopObserving({ nav.navigateUp() }) },
                create = { name, email, password ->
                    vm.createUser(
                        name = name,
                        email = email,
                        password = password,
                        onSuccess = { nav.navigate(SearchTablesRoute){ popUpTo(IdleRoute) { inclusive = false } } }
                    )
                }
            )
        }

        composable<SearchTablesRoute> {

            val user = ui.user ?: return@composable

            LaunchedEffect(Unit) {
                vm.getTables()
                vm.observeLobby()
            }

            SearchTablesScreen(
                user = user,
                tables = ui.tables,
                mainMenuNav = { vm.stopObserving({ nav.navigateUp() }) },
                joinTable = { table ->
                    vm.joinTable(
                        table = table,
                        onSuccess = { nav.navigate(WaitingTableRoute) }
                    )
                },
                createTable = { name ->
                    vm.createTable(
                        tableName = name,
                        onSuccess = { nav.navigate(WaitingTableRoute) }
                    )
                }
            )
        }

        composable<WaitingTableRoute> {

            val user = ui.user ?: return@composable
            val table = ui.currentTable ?: return@composable

            LaunchedEffect(Unit) {
                vm.observeTable(table.name.string)
            }

            WaitingTableScreen(
                user = user,
                table = table,
                changeRole = { vm.changeRole(table.name.string) },
                leaveTable = { vm.leaveTable(table.name.string) { nav.popBackStack() } }
            )
        }

    }

    ui.errorMessage?.let { message ->
        DisplayError(
            error = message,
            onDismiss = vm::dismissError
        )
    }
}
