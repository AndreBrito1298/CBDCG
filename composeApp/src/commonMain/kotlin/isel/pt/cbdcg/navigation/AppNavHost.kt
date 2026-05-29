package isel.pt.cbdcg.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import isel.pt.cbdcg.viewmodel.AppViewModel
import isel.pt.cbdcg.viewmodel.SessionState
import isel.pt.cbdcg.views.game.GameScreen
import isel.pt.cbdcg.views.game.SpectatorScreen
import isel.pt.cbdcg.views.lobby.SearchTablesScreen
import isel.pt.cbdcg.views.lobby.WaitingTableScreen
import isel.pt.cbdcg.views.startMenu.CreateUserScreen
import isel.pt.cbdcg.views.startMenu.MenuScreen
import isel.pt.cbdcg.views.startMenu.LoginScreen
import isel.pt.cbdcg.views.utils.DisplayError

private enum class AppDestination {
    Menu,
    Lobby,
    Table,
    Game
}

@Composable
fun AppNavHost(vm: AppViewModel) {

    val nav = rememberNavController()
    val ui by vm.ui.collectAsState()

    val destination = when (ui.session) {
        SessionState.SignedOut -> AppDestination.Menu
        is SessionState.InLobby -> AppDestination.Lobby
        is SessionState.InTable -> AppDestination.Table
        is SessionState.InGame -> AppDestination.Game
    }

    LaunchedEffect(destination) {

        when(destination){

            AppDestination.Game -> {
                nav.navigate(GameRoute) {
                    popUpTo(SearchTablesRoute) { inclusive = false }
                    launchSingleTop = true
                }
            }
            AppDestination.Lobby -> {
                nav.navigate(SearchTablesRoute) {
                    popUpTo(MenuRoute) { inclusive = false } // Ignore Login/Creation menu
                    launchSingleTop = true
                }
            }
            AppDestination.Table -> {
                nav.navigate(WaitingTableRoute) {
                    launchSingleTop = true
                }
            }
            AppDestination.Menu -> {
                nav.navigate(MenuRoute) {
                    popUpTo(0)
                    launchSingleTop = true
                }
            }

        }
    }

    // Available Routes
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
                mainMenuNav = { nav.navigate(MenuRoute) },
                login = { email, password ->
                    vm.login(
                        email = email,
                        password = password,
                    )
                }
            )
        }

        composable<CreateUserRoute> {
            CreateUserScreen(
                mainMenuNav = { nav.navigate(MenuRoute) },
                create = { name, email, password ->
                    vm.createUser(
                        name = name,
                        email = email,
                        password = password,
                    )
                }
            )
        }

        composable<SearchTablesRoute> {

            val session = ui.session as? SessionState.InLobby ?: return@composable

            LaunchedEffect(Unit) {
                vm.getTables()
                vm.observeLobby()
            }

            SearchTablesScreen(
                user = session.user,
                tables = session.tables,
                joinTable = { table -> vm.joinTable(table = table) },
                createTable = { name -> vm.createTable(tableName = name) },
                logout = vm::logout
            )
        }

        composable<WaitingTableRoute> {

            val session = ui.session as? SessionState.InTable ?: return@composable

            LaunchedEffect(Unit) {
                vm.observeTable(session.table.name.string)
            }

            WaitingTableScreen(
                user = session.user,
                table = session.table,
                changeRole = { role -> vm.changeRole(role) },
                leaveTable = vm::leaveTable,
                createGame = vm::createGame
            )

        }

        composable<GameRoute> {

            val session = ui.session as? SessionState.InGame ?: return@composable

            val player = session.game.players.find{ it.user.id == session.user.id }
            val spectator = session.game.spectators.find{ it.user.id == session.user.id }

            if(player == null && spectator == null) return@composable

            LaunchedEffect(Unit) {
                vm.observeGame(session.game.id)
            }

            if(player != null){
                GameScreen(
                    player = player,
                    game = session.game,
                    gameUI = ui.gameUI,
                    selectCard = { idx, card -> vm.selectCard(idx, card) },
                    placeSignal = vm::placeSignal,
                    placeOnBoard = { pos -> vm.placeOnBoard(pos) },
                    selectBoardCharacter = { tile -> vm.selectBoardCharacter(tile) },
                    toggleCardStats = { card -> vm.inspectCard(card) },
                    moveSignal = vm::moveSignal,
                    moveCharacter = { tile -> vm.moveCharacter(tile) },
                    rotateTile = { flag -> vm.rotateTile(flag) },
                    zoom = { option -> vm.zoom(option) },
                    nextPhase = vm::nextPhase,
                )
            }

            if(spectator != null){
                SpectatorScreen(
                    spectator = spectator,
                    game = session.game,
                    gameUI = ui.gameUI,
                    togglePlayerHand = { player -> vm.inspectPlayerHand(player) },
                    selectBoardCharacter = { tile -> vm.selectBoardCharacter(tile) },
                    toggleCardStats = { card -> vm.inspectCard(card) },
                    zoom = { option -> vm.zoom(option) }
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
