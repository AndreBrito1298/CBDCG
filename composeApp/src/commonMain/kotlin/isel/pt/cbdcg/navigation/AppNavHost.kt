package isel.pt.cbdcg.navigation

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.viewmodel.AppViewModel
import isel.pt.cbdcg.viewmodel.GameUIState
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
        is SessionState.SignedOut -> AppDestination.Menu
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
                    unequip = { idx -> vm.unequip(idx) },
                    toggleCardStats = { card, boardTile -> vm.inspectCard(card, boardTile) },
                    onEffectInfoClick = {
                        // Esta lógica não vai ficar aqui
                        val effect = (ui.gameUI.state as? GameUIState.InspectTileEffect)?.tile?.specialEffect?.type
                            ?: return@GameScreen
                        when (effect) {
                            is TileEffectTypes.Chest, is TileEffectTypes.BigChest -> vm.drawItem()
                            is TileEffectTypes.StatUp, is TileEffectTypes.StatDown,
                            is TileEffectTypes.StatUpAoE, is TileEffectTypes.StatDownAoE -> vm.statModifierEffect()

                            else -> return@GameScreen
                        }
                        // É temporário, para efeitos de testes.
                    },
                    moveSignal = { boardTile -> vm.moveSignal(boardTile) },
                    moveCharacter = { tile -> vm.moveCharacter(tile) },
                    battleSignal = { current, target -> vm.collisionSignal(current, target) },
                    challenge = vm::challenge,
                    sneak = vm::sneaking,
                    rotateTile = { flag -> vm.rotateTile(flag) },
                    zoom = { option -> vm.zoom(option) },
                    nextPhase = vm::nextPhase,
                    closeDialog = { inBattle -> if(inBattle) vm.backToBattle() else vm.idle() },
                    endBattle = vm::leaveBattle,
                    attackTarget = { target -> if(target == null) vm.attackMode() else vm.chooseTarget(target) },
                    battleAction = { action ->
                        when(action){
                            PossibleBattleActions.HOLD -> vm.actInBattle(PossibleBattleActions.HOLD)
                            PossibleBattleActions.FLEE -> vm.actInBattle(PossibleBattleActions.FLEE)
                            PossibleBattleActions.ATTACK -> vm.attack()
                            null -> vm.undoBattleAction()
                        }
                    },
                    participateInBattle = { accept -> vm.participateInBattle(accept) },
                    leaveGame = vm::leaveGame,
                    getDrawable = { vm.getDrawable(it) },
                )
            }

            if(spectator != null){
                SpectatorScreen(
                    spectator = spectator,
                    game = session.game,
                    gameUI = ui.gameUI,
                    togglePlayerHand = { player -> vm.inspectPlayerHand(player) },
                    toggleCardStats = { card, boardTile -> vm.inspectCard(card, boardTile) },
                    onEffectInfoClick = vm::idle,
                    zoom = { option -> vm.zoom(option) },
                    leaveGame = vm::leaveGame,
                    getDrawable = { vm.getDrawable(it) },
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
