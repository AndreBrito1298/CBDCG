package isel.pt.cbdcg.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import isel.pt.cbdcg.AppViewModel
import isel.pt.cbdcg.views.game.GameScreen
import isel.pt.cbdcg.views.game.SpectatorScreen
import isel.pt.cbdcg.views.lobby.SearchTablesScreen
import isel.pt.cbdcg.views.lobby.WaitingTableScreen
import isel.pt.cbdcg.views.startMenu.CreateUserScreen
import isel.pt.cbdcg.views.startMenu.MenuScreen
import isel.pt.cbdcg.views.startMenu.LoginScreen
import isel.pt.cbdcg.views.utils.DisplayError


@Composable
fun AppNavHost(vm: AppViewModel) {

    val nav = rememberNavController()
    val ui by vm.ui.collectAsState()

    NavHost(
        navController = nav,
        startDestination = MenuRoute
    ){

        composable<MenuRoute> {
            MenuScreen(
                loginNav = { nav.navigate(LoginRoute) },
                createUserNav = { nav.navigate(CreateUserRoute) },
            )
        }

        composable<LoginRoute> {

            LoginScreen(
                mainMenuNav = { vm.stopObserving { nav.navigateUp() } },
                login = { email, password ->
                    vm.login(
                        email = email,
                        password = password,
                        onSuccess = { nav.navigate(SearchTablesRoute){ popUpTo(MenuRoute) { inclusive = false } } }
                    )
                }
            )
        }

        composable<CreateUserRoute> {
            CreateUserScreen(
                mainMenuNav = { vm.stopObserving { nav.navigateUp() } },
                create = { name, email, password ->
                    vm.createUser(
                        name = name,
                        email = email,
                        password = password,
                        onSuccess = { nav.navigate(SearchTablesRoute){ popUpTo(MenuRoute) { inclusive = false } } }
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
                },
                logout = {
                    vm.logout(
                        onSuccess = {
                            nav.popBackStack(MenuRoute, inclusive = false)
                        }
                    )
                }
            )
        }

        composable<WaitingTableRoute> {

            val user = ui.user ?: return@composable
            val table = ui.currentTable
            val game = ui.game

            // If the table was deleted
            LaunchedEffect(table) {
                if (table == null) {
                    nav.navigate(SearchTablesRoute) {
                        popUpTo(WaitingTableRoute) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            }

            if(table == null) return@composable

            LaunchedEffect(game) {
                if (game != null) {
                    nav.navigate(GameRoute)
                }
            }

            LaunchedEffect(Unit) {
                vm.observeTable(table.name.string)
            }

            WaitingTableScreen(
                user = user,
                table = table,
                changeRole = { role -> vm.changeRole(role) },
                leaveTable = { vm.leaveTable { nav.popBackStack() } },
                createGame = { vm.createGame { nav.navigate(GameRoute) } }
            )

        }

        composable<GameRoute> {

            val user = ui.user ?: return@composable
            val game = ui.game ?: return@composable
            val player = ui.game?.players?.find { it.user == user.id }
            val spectator = ui.game?.spectators?.find{ it.user == user.id }

            if(player == null && spectator == null) return@composable
            else{

                LaunchedEffect(Unit) {
                    vm.observeGame(game.id)
                }

                if(player != null)
                    GameScreen(
                        player = player,
                        game = game,
                        placeTile = { tile, idx, pos -> vm.placeTile(tile, idx, pos) },
                        rotateTile = { idx, flag -> vm.rotateTile(idx, flag) }
                    )

                if(spectator != null)
                    SpectatorScreen(
                        game = game
                    )
            }
        }
    }

    ui.errorMessage?.let { message ->
        DisplayError(
            error = message,
            onDismiss = vm::dismissError
        )
    }
}
