package isel.pt.cbdcg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isel.pt.cbdcg.MIN_SNEAK_CHANCE
import isel.pt.cbdcg.SNEAK_BASE_CHANCE
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CardType
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.connectedNeighbours
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.findPath
import isel.pt.cbdcg.domain.game.board.tile.rotate
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.adjustStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.contains
import kotlin.random.Random

data class AppUIState(
    val session: SessionState = SessionState.SignedOut,
    val gameUI: GameUI = GameUI(GameUIState.Idle),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class AppViewModel(
    val clientApi: ClientApi,
): ViewModel() {

    val ui: StateFlow<AppUIState>
        field = MutableStateFlow(AppUIState())

    init {
        
        // Viewmodel must be ready to react to updates in the clientApi

        viewModelScope.launch {
            clientApi.tables.collect { tables -> updateLobby(tables) }
        }
        viewModelScope.launch {
            clientApi.currentTable.collect { table -> if(table == null) updateTableRemoved() else updateTable(table) }
        }
        viewModelScope.launch {
            clientApi.game.collect { game -> if(game == null) updateGameRemoved() else updateGame(game) }
        }
    }

    private fun updateLobby(newTables: List<Table>) =
        ui.update { ui ->
            val session = ui.session
            if(session is SessionState.InLobby)
                ui.copy(session= session.copy(tables = newTables))
            else ui
        }
    private fun updateTableRemoved() =
        ui.update{ ui ->
            val session = ui.session
            if(session is SessionState.InTable)
                ui.copy(session = SessionState.InLobby(session.user))
            else ui
        }
    private fun updateTable(table: Table) =
        ui.update{ ui ->
            val session = ui.session
            if(session is SessionState.InTable && table.participants.any{ it.user.id == session.user.id })
                ui.copy(session = session.copy(table = table))
            else ui
        }
    private fun updateGameRemoved() =
        ui.update{ ui ->
            val session = ui.session
            if(session is SessionState.InGame)
                ui.copy(session = SessionState.InLobby(session.user))
            else ui
        }
    private fun updateGame(game: Game) =
        ui.update{ ui ->
            when (val session = ui.session) {
                is SessionState.InTable ->
                    ui.copy(session = SessionState.InGame(session.user, game))
                is SessionState.InGame ->
                    ui.copy(
                        session = session.copy(game = game),
                        gameUI = updateInGameState(session.user, game, ui.session.game, ui.gameUI)
                    )
                else ->
                    ui
            }
        }
    private fun updateInGameState(user: User, newGame: Game, currentGame: Game, currentUI: GameUI): GameUI {

        val ui =
            if(newGame.turn.playerTurn != currentGame.turn.playerTurn) currentUI.copy(charactersBattled = emptyList())
            else currentUI

        if(newGame.players.size == 1) return ui.copy(state = GameUIState.GameOver(newGame.players.first()))

        val battle = newGame.battle
        if(battle != null){

            val me = newGame.players.find{ it.user.id == user.id } ?: return ui

            return when(ui.state){

                is GameUIState.Attacking,
                is GameUIState.EndBattle,
                is GameUIState.InBattle -> updateBattleEnd(me, newGame, battle, ui)

                else -> updateBattleStart(me, newGame, battle, ui)
            }

        }
        if(currentGame.battle != null){
            return ui.copy(
                state = GameUIState.Idle,
            )
        }

        return ui
    }
    private fun updateBattleEnd(player: Player, game: Game, battle: Battle, ui: GameUI): GameUI{

        val remainingCharacters = battle.characters.filter{ it.adjustStats().hp > 0 }

        return if(remainingCharacters.size == 1) {
            val playersInBattle = game.players.filter { it.currentCharacter in battle.characters.map { it.name } }
            val playerCharacter = game.board.tiles.first { it.character?.name == player.currentCharacter }.character
            val winner = playersInBattle.first { it.currentCharacter == remainingCharacters.first().name }
            val losers = playersInBattle.filter { it.currentCharacter != winner.currentCharacter }
            val readyToLeave = game.players.filter{ player -> player.currentCharacter in battle.pending.map{ it.origin.name } }

            ui.copy(state = GameUIState.EndBattle(requireNotNull(playerCharacter), winner, losers, battle.itemBet, readyToLeave))
        }
        else ui.copy(state = GameUIState.InBattle(player, battle))
    }
    private fun updateBattleStart(player: Player, game: Game, battle: Battle, ui: GameUI): GameUI {

        val myCharacter = game.board.tiles.find{ boardTile ->
            val character = boardTile.character
            character != null && character.name == player.currentCharacter
        }?.character ?: return ui

        return if(battle.currentTurn > 0u) ui.copy(state = GameUIState.InBattle(player, battle))
               else if(myCharacter in battle.characters) ui.copy(state = GameUIState.StartBattle(battle, myCharacter))
               else ui.copy(state = GameUIState.Idle)
    }

    fun dismissError() {

        val nextGameUIState = when(val state = ui.value.gameUI.state) {
            is GameUIState.MovingCharacter, is GameUIState.PlacingCard,
            is GameUIState.SelectCard, is GameUIState.SneakDestination -> GameUIState.Idle
            else -> state
        }

        ui.update { it.copy(errorMessage = null, gameUI = it.gameUI.copy(state = nextGameUIState)) }
    }

    // Websocket-related operations

    fun observeLobby(): Job = viewModelScope.launch {
        clientApi.subscribeLobby()
    }
    fun observeTable(tableName: String): Job = viewModelScope.launch {
        clientApi.subscribeTable(tableName)
    }
    fun observeGame(gameId: UInt): Job = viewModelScope.launch{
        clientApi.subscribeGame(gameId)
    }
    fun stopObserving(): Job =
        viewModelScope.launch {
            clientApi.disconnectAll()
        }

    // User-related operations

    fun login(email: String, password: String): Job? {

        val session = ui.value.session
        if(session !is SessionState.SignedOut) return null

        return viewModelScope.launch {

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            val email = Email(email)
            val password = Password(password)

            clientApi.login(email, password).fold(
                onSuccess = { user ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = SessionState.InLobby(user, emptyList())
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Login Failed."
                        )
                    }
                }
            )
        }
    }
    fun logout(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        val user = session.user
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.logout(token).fold(
                onSuccess = {
                    stopObserving()
                    ui.value = AppUIState()
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Logout Failed."
                        )
                    }
                }
            )
        }
    }
    fun createUser(name: String, email: String, password: String): Job? {

        val session = ui.value.session
        if(session !is SessionState.SignedOut) return null

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            val name = Name(name)
            val email = Email(email)
            val password = Password(password)

            clientApi.createUser(name, email, password).fold(
                onSuccess = { user ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = SessionState.InLobby(user, emptyList())
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Create User Failed."
                        )
                    }
                }
            )
        }
    }

    // Table-related operations

    fun getTables(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.getTables().fold(
                onSuccess = { tables ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(tables = tables)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Could not load tables."
                        )
                    }
                }
            )
        }
    }
    fun joinTable(table: Table): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        val user = session.user

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.joinTable(user.id, table.id).fold(
                onSuccess = { table ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = SessionState.InTable(session.user, table)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Join Table Failed."
                        )
                    }
                }
            )
        }
    }
    fun createTable(tableName: String): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        val user = session.user

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            val name = Name(tableName)
            val id = user.id

            clientApi.createTable(name, id).fold(
                onSuccess = { table ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = SessionState.InTable(session.user, table)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Create Table Failed."
                        )
                    }
                }
            )
        }
    }
    fun changeRole(role: Role): Job? {

        val session = ui.value.session
        if(session !is SessionState.InTable) return null

        val user = session.user
        val table = session.table

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.changeRole(user.id, table.id, role).fold(
                onSuccess = {
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Change Role Failed."
                        )
                    }
                }
            )
        }

    }
    fun leaveTable(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InTable) return null

        val user = session.user
        val table = session.table

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.leaveTable(user.id, table.id).fold(
                onSuccess = {
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = SessionState.InLobby(session.user, emptyList())
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Leave Table Failed."
                        )
                    }
                }
            )
        }
    }

    // Game-related operations

    fun createGame(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InTable) return null

        val user = session.user
        val table = session.table

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.createGame(table.id, user.id).fold(
                onSuccess = { game ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = SessionState.InGame(session.user, game),
                            gameUI = it.gameUI.copy(state = GameUIState.Idle)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Create Game Failed."
                        )
                    }
                }
            )
        }
    }
    fun placeOnBoard(pos: BoardPosition): Job? {

        val session = ui.value.session
        val gameState = ui.value.gameUI.state
        if(session !is SessionState.InGame) return null
        if(gameState !is GameUIState.PlacingCard) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.placeOnBoard(user.id, game.id, token, gameState.card, gameState.idx, pos).fold(
                onSuccess = { newGame ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(state = GameUIState.Idle)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Place Card Failed."
                        )
                    }
                }
            )
        }
    }
    fun rotateTile(right: Boolean): Job? {

        val session = ui.value.session
        val gameState = ui.value.gameUI.state
        if(session !is SessionState.InGame) return null
        if(gameState !is GameUIState.SelectCard || gameState.card.type != CardType.TILE) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.rotateTile(user.id, game.id, token, gameState.idx, right).fold(
                onSuccess = { newGame ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(
                                state = gameState.copy(card = TileCard((gameState.card as TileCard).tile.rotate(right)))
                            )
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Rotate Tile Failed."
                        )
                    }
                }
            )
        }

    }
    fun nextPhase(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InGame) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.nextPhase(user.id, game.id, token).fold(
                onSuccess = { newGame ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(state = GameUIState.Idle, movementUsed = 0)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't skip phase."
                        )
                    }
                }
            )
        }
    }
    fun selectCard(idx: UInt, card: Card): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val selected = gameUI.state is GameUIState.SelectCard && gameUI.state.idx == idx

        return viewModelScope.launch{
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state =
                        if(selected) GameUIState.Idle
                        else GameUIState.SelectCard(idx, card)
                    )
                )
            }
        }
    }
    fun placeSignal(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.SelectCard) return null

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = GameUIState.PlacingCard(gameUI.state.idx, gameUI.state.card)
                    )
                )
            }
        }

    }
    fun inspectCard(card: Card?, boardTile: BoardTile? = null): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val previous = (gameUI.state as? GameUIState.InspectCard)?.previous
        val nextState =
            when (card) {
                null -> previous as? GameUIState.InspectPlayer ?: GameUIState.Idle
                is TileCard -> GameUIState.InspectTileEffect(card.tile, boardTile)
                else -> GameUIState.InspectCard(card, gameUI.state)
            }

        return viewModelScope.launch{
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state = nextState)
                )
            }
        }

    }
    fun unequip(idx: Int): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.InspectCard) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val character = (gameUI.state.card as? CharacterCard)?.character ?: return null.also {
            ui.update { it.copy(errorMessage = "This isn't a Character") }
        }

        return viewModelScope.launch {

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.unequipItem(user.id, game.id, token, character, idx).fold(
                onSuccess = { newGame ->

                    val newCharacterInfo = newGame.board.tiles.find{ it.character?.name == character.name }?.character

                    if(newCharacterInfo == null)
                        ui.update { it.copy(isLoading = false, errorMessage = "Couldn't find Character") }
                    else
                        ui.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                session = session.copy(game = newGame),
                                gameUI = gameUI.copy(state = gameUI.state.copy(card = CharacterCard(newCharacterInfo)))
                            )
                        }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Unequip Item Failed."
                        )
                    }
                }
            )
        }
    }
    fun zoom(option: Boolean): Job?{

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val newZoom =
            if(option) (gameUI.boardZoom + 0.25f).coerceAtMost(2f)
            else (gameUI.boardZoom - 0.25f).coerceAtLeast(0.5f)

        return viewModelScope.launch{
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(boardZoom = newZoom)
                )
            }
        }
    }
    fun idle(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        return viewModelScope.launch{
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state = GameUIState.Idle)
                )
            }
        }
    }
    fun inspectPlayerHand(player: Player): Job?{

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val selected = gameUI.state is GameUIState.InspectPlayer && gameUI.state.player == player

        return viewModelScope.launch{
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state =
                        if(selected) GameUIState.Idle
                        else GameUIState.InspectPlayer(player))
                )
            }
        }
    }
    fun moveSignal(boardTile: BoardTile): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = GameUIState.MovingCharacter(
                            from = boardTile,
                            path = emptyList()
                        )
                    )
                )
            }
        }
    }
    fun leaveGame(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InGame) return null

        /*


        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        */

        return viewModelScope.launch{

            ui.update{
                it.copy(errorMessage = null, session = SessionState.InLobby(session.user, emptyList()))
            }

            /* Isto é mais para "expulsar" do que para "sair"

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.leaveGame(user.id, game.id, token)

            response.onSuccess {
                ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InLobby(session.user, emptyList())
                    )
                }
            }
            response.onFailure { error ->
                ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Leave Game Failed."
                    )
                }
            }

            */
        }
    }
    
    fun moveCharacter(to: BoardTile): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        if(gameUI.state is GameUIState.SneakDestination) return normalMovement(session, gameUI.state.origin, to, 2)
        if(gameUI.state !is GameUIState.MovingCharacter) return null

        val makeMovement = gameUI.state.path.lastOrNull() == to

        return if(makeMovement && to.character == null) normalMovement(session, gameUI.state.from, to, gameUI.state.path.size - 1)
        else if(makeMovement && to.character != null) moveToStartBattle(session, gameUI, to)
        else movementPath(session, gameUI, to)
    }
    private fun normalMovement(session: SessionState.InGame, from: BoardTile, to: BoardTile, steps: Int): Job?{

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.moveCharacter(
                user.id,
                game.id,
                token,
                from,
                to,
            ).fold(
                onSuccess = { newGame ->

                    val boardTile = newGame.board.tiles.firstOrNull { boardTile ->
                        boardTile.character != null && boardTile.character == from.character
                    }
                    val specialEffect = boardTile?.tile?.specialEffect?.type
                    val nextState =
                        if (specialEffect != null && boardTile.cooldown == 0u && specialEffect != TileEffectTypes.None && specialEffect != TileEffectTypes.Start)
                            GameUIState.InspectTileEffect(boardTile.tile, boardTile, activateInTile = true)
                        else GameUIState.Idle

                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(
                                state = nextState,
                                movementUsed = it.gameUI.movementUsed + steps
                            )
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't move character."
                        )
                    }
                }
            )
        }
    }
    private fun moveToStartBattle(session: SessionState.InGame, gameUI: GameUI, to: BoardTile): Job? {

        if (gameUI.state !is GameUIState.MovingCharacter) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val character = (gameUI.state.from.character as? PlayableCharacter) ?: return null.also {
            ui.update { it.copy(errorMessage = "There is no character in the tile clicked.") }
        }
        val enemy = to.character ?: return null.also {
            ui.update { it.copy(errorMessage = "There is no character to battle against.") }
        }

        return viewModelScope.launch {

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.moveCharacter(
                user.id,
                game.id,
                token,
                gameUI.state.path.first(),
                gameUI.state.path.dropLast(1).last(),
            ).fold(
                onSuccess = { newGame ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(
                                state = GameUIState.CharacterCollision(character, enemy),
                                movementUsed = gameUI.movementUsed + gameUI.state.path.size - 2
                            )
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't move character."
                        )
                    }
                }
            )
        }
    }
    private fun movementPath(session: SessionState.InGame, gameUI: GameUI, to: BoardTile): Job? {

        if (gameUI.state !is GameUIState.MovingCharacter) return null

        val characterSpe = gameUI.state.from.character?.adjustStats()?.spe ?: 0
        val remainingMovement = characterSpe - gameUI.movementUsed
        if(remainingMovement <= 0) return null.also {
            ui.update { it.copy(errorMessage = "This character cannot move this turn anymore.") }
        }

        val path = session.game.board.findPath(
            from = gameUI.state.from,
            to = to,
            maxDistance = remainingMovement,
            ignoreCharacters = gameUI.charactersBattled
        )

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = gameUI.state.copy(path = path)
                    )
                )
            }
        }
        
    }
    fun collisionSignal(current: Character, target: Character): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = GameUIState.CharacterCollision(
                            playerCharacter = current,
                            enemyCharacter = target,
                        )
                    )
                )
            }
        }
    }
    fun challenge(swap: Boolean = false): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.CharacterCollision) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val (attacker, defender) =
            if(!swap) gameUI.state.playerCharacter to gameUI.state.enemyCharacter
            else gameUI.state.enemyCharacter to gameUI.state.playerCharacter

        return viewModelScope.launch {

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.challenge(user.id, game.id, token, attacker, defender).fold(
                onSuccess = { newGame ->

                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                        )
                    }
                },
                onFailure = { error ->
                    ui.update { it.copy(isLoading = false, errorMessage = error.message) }
                }
            )
        }

    }
    fun sneaking(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.CharacterCollision) return null

        if(gameUI.state.playerCharacter.adjustStats().spe - gameUI.movementUsed < 2) return null.also {
            ui.update { it.copy(errorMessage = "You don't have enough movement to sneak past the enemy.") }
        }

        val speDiff = (gameUI.state.playerCharacter.adjustStats().spe - gameUI.state.enemyCharacter.adjustStats().spe)
            .coerceAtLeast(MIN_SNEAK_CHANCE)

        val sneakChance = SNEAK_BASE_CHANCE + (speDiff / 10)
        if(Random.nextFloat() > sneakChance) return challenge(true)

        val origin = session.game.board.tiles.firstOrNull { it.character == gameUI.state.playerCharacter }
            ?: return null.also { ui.update { it.copy(errorMessage = "Your character couldn't be found on the board.") } }

        val enemyTile = session.game.board.tiles.firstOrNull { it.character == gameUI.state.enemyCharacter }
            ?: return null.also { ui.update { it.copy(errorMessage = "The enemy character couldn't be found on the board.") } }

        val targets = session.game.board
            .connectedNeighbours(enemyTile)
            .filter{ it.character == null }


        return viewModelScope.launch {
            ui.update { it.copy(errorMessage = null, gameUI = it.gameUI.copy(state = GameUIState.SneakDestination(origin, targets))) }
        }
    }
    fun drawItem(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.InspectTileEffect) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val boardTile = gameUI.state.boardTile ?: return null.also{
            ui.update { it.copy(errorMessage = null, gameUI = gameUI.copy(state = GameUIState.Idle)) }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.drawItem(
                user.id,
                game.id,
                token,
                boardTile
            ).fold(
                onSuccess = { newGame ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(state = GameUIState.Idle)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't draw Item."
                        )
                    }
                }
            )
        }
    }
    fun statModifierEffect(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.InspectTileEffect) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        if(!gameUI.state.activateInTile) return null.also{
            ui.update { it.copy(errorMessage = null, gameUI = gameUI.copy(state = GameUIState.Idle)) }
        }

        val boardTile = gameUI.state.boardTile ?: return null.also{
            ui.update { it.copy(errorMessage = "No Board Tile found.") }
        }

        return viewModelScope.launch {

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.statModifierEffect(user.id, game.id, token, boardTile).fold(
                onSuccess = { newGame ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(state = GameUIState.Idle)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't apply Effect."
                        )
                    }
                }
            )
        }
    }
    fun attackMode(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.InBattle) return null

        val firstCharacter = gameUI.state.battle.characters.firstOrNull{
            it.name != gameUI.state.player.currentCharacter
        } ?: return null.also{
            ui.update { it.copy(errorMessage = "No other character found.") }
        }

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state = GameUIState.Attacking(gameUI.state.player, gameUI.state.battle, firstCharacter))
                )
            }
        }
    }
    fun chooseTarget(target: Character): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.Attacking) return null

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state = gameUI.state.copy(target = target))
                )
            }
        }
    }
    fun backToBattle(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.Attacking) return null

        return viewModelScope.launch {
            ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state = GameUIState.InBattle(gameUI.state.player, gameUI.state.battle))
                )
            }
        }
    }
    fun attack(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.Attacking) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val origin = gameUI.state.battle.characters.find{ it.name == gameUI.state.player.currentCharacter } ?: return null.also {
            ui.update { it.copy(errorMessage = "No Character found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.actInBattle(user.id, game.id, token, PossibleBattleActions.ATTACK, origin, gameUI.state.target).fold(
                onSuccess = { newGame ->
                    ui.update{

                        val battle = newGame.battle
                            ?: return@update it.copy(isLoading = false, errorMessage = "The battle couldn't be found.")

                        val remainingCharacters = battle.characters.count{ it.adjustStats().hp > 0 }
                        val nextState =
                            if(remainingCharacters != 1) GameUIState.InBattle(gameUI.state.player, battle)
                            else{
                                val winnerCharacter = battle.characters.first{ it.adjustStats().hp > 0 }
                                val winnerPlayer = session.game.players.find{
                                    it.currentCharacter == winnerCharacter.name
                                } ?: return@update it.copy(isLoading = false, errorMessage = "The winner couldn't be found.")
                                val loserPlayers = session.game.players.filter{ it.currentCharacter != winnerCharacter.name }
                                val readyToLeave = newGame.players.filter{ player -> player.currentCharacter in battle.pending.map{ it.origin.name } }

                                GameUIState.EndBattle(origin, winnerPlayer, loserPlayers, battle.itemBet, readyToLeave)
                            }

                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = gameUI.copy(state = nextState)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't ATTACK."
                        )
                    }
                }
            )
        }
    }
    fun participateInBattle(accept: Boolean): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.StartBattle) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.participateInBattle(user.id, game.id, token, gameUI.state.character, accept).fold(
                onSuccess = { newGame ->
                    ui.update{
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't confirm or leave the battle."
                        )
                    }
                }
            )
        }
    }
    fun actInBattle(action: PossibleBattleActions): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.InBattle) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val origin = gameUI.state.battle.characters.find{ it.name == gameUI.state.player.currentCharacter } ?: return null.also {
            ui.update { it.copy(errorMessage = "No Character found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.actInBattle(user.id, game.id, token, action, origin).fold(
                onSuccess = { newGame ->
                    ui.update{
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't ${action.name}."
                        )
                    }
                }
            )
        }
    }
    fun undoBattleAction(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.InBattle) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        val origin = gameUI.state.battle.characters.find{ it.name == gameUI.state.player.currentCharacter } ?: return null.also {
            ui.update { it.copy(errorMessage = "No Character found.") }
        }

        return viewModelScope.launch {
            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.undoBattleAction(user.id, game.id, token, origin).fold(
                onSuccess = { newGame ->
                    ui.update{
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame)
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't UNDO your character's action."
                        )
                    }
                }
            )
        }
    }
    fun leaveBattle(): Job?{

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.EndBattle) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            ui.update { it.copy(isLoading = true, errorMessage = null) }

            clientApi.leaveBattle(user.id, game.id, token, gameUI.state.playerCharacter).fold(
                onSuccess = { newGame ->
                    ui.update{
                        it.copy(
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(
                                charactersBattled =
                                    if(gameUI.state.winner.user.id == user.id && newGame.turn.playerTurn.first() == user.id)
                                        (gameUI.state.losers + gameUI.state.winner).mapNotNull{ player ->
                                            if(player.user.id != user.id) player.currentCharacter
                                            else null
                                        }
                                    else gameUI.charactersBattled
                            )
                        )
                    }
                },
                onFailure = { error ->
                    ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't leave the battle."
                        )
                    }
                }
            )
        }
    }
}