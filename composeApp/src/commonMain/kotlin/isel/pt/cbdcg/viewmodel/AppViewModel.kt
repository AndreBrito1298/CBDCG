package isel.pt.cbdcg.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CardType
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.findPath
import isel.pt.cbdcg.domain.game.board.rotate
import isel.pt.cbdcg.views.game.utils.cardInfo.adjustStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
data class AppUIState(
    val session: SessionState = SessionState.SignedOut,
    val gameUI: GameUI = GameUI(GameUIState.Idle),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)


class AppViewModel(
    val clientApi: ClientApi,
): ViewModel() {

    // Colocar o _ui no estado? - explicitFields
    /*
    The feature "explicit backing fields" is experimental and should be enabled explicitly.
    This can be done by supplying the compiler argument '-Xexplicit-backing-fields', but note that no
    stability guarantees are provided.
    */
    private val _ui = MutableStateFlow<AppUIState>(AppUIState())
    val ui = _ui.asStateFlow()

    init {

        // Viewmodel must be ready to react to updates in the clientApi

        viewModelScope.launch {
            clientApi.tables.collect { tables ->
                _ui.update { ui ->
                    val session = ui.session

                    if(session is SessionState.InLobby)
                        ui.copy(session= session.copy(tables = tables))
                    else ui
                }
            }
        }
        viewModelScope.launch {
            clientApi.currentTable.collect { table ->
                _ui.update { ui ->
                    when (val session = ui.session) {
                        is SessionState.InTable if table != null && table.participants.any { it.user.id == session.user.id } ->
                            ui.copy(session = session.copy(table = table))

                        is SessionState.InTable ->
                            ui.copy(session = SessionState.InLobby(session.user))

                        else -> ui
                    }

                }
            }
        }
        viewModelScope.launch {
            clientApi.game.collect { game ->
                _ui.update { ui ->
                    val session = ui.session

                    when (session) {
                        is SessionState.InGame if game != null ->
                            ui.copy(session = session.copy(game = game))

                        is SessionState.InTable if game != null ->
                            ui.copy(session = SessionState.InGame(session.user, game))

                        else -> ui
                    }
                }
            }
        }
    }

    fun dismissError() {
        _ui.update { it.copy(errorMessage = null, gameUI = it.gameUI.copy(state= GameUIState.Idle)) }
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

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val email = Email(email)
            val password = Password(password)

            val response = clientApi.login(email, password)

            response.onSuccess { user ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InLobby(user, emptyList())
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Login Failed."
                    )
                }
            }
        }
    }
    fun logout(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        val user = session.user
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.logout(token)
            response.onSuccess {
                stopObserving()
                _ui.value = AppUIState()
            }
            response.onFailure { error ->
                _ui.update{
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Logout Failed."
                    )
                }
            }
        }
    }
    fun createUser(name: String, email: String, password: String): Job? {

        val session = ui.value.session
        if(session !is SessionState.SignedOut) return null

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val name = Name(name)
            val email = Email(email)
            val password = Password(password)

            val response = clientApi.createUser(name, email, password)

            response.onSuccess { user ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InLobby(user, emptyList())
                    )
                }
            }
            response.onFailure { error ->
                _ui.update{
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Create User Failed."
                    )
                }
            }
        }
    }

    // Table-related operations

    fun getTables(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        return viewModelScope.launch {

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.getTables()

            response.onSuccess { tables ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(tables = tables)
                        )
                    }
                }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Could not load tables."
                    )
                }
            }
        }
    }
    fun joinTable(table: Table): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        val user = session.user
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.joinTable(user.id, table.id, token)

            response.onSuccess { table ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InTable(session.user, table)
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Join Table Failed."
                    )
                }
            }
        }
    }
    fun createTable(tableName: String): Job? {

        val session = ui.value.session
        if(session !is SessionState.InLobby) return null

        val user = session.user
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val name = Name(tableName)
            val id = user.id

            val response = clientApi.createTable(name, id, token)

            response.onSuccess { table ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InTable(session.user, table)
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Create Table Failed."
                    )
                }
            }
        }
    }
    fun changeRole(role: Role): Job? {

        val session = ui.value.session
        if(session !is SessionState.InTable) return null

        val user = session.user
        val table = session.table
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.changeRole(user.id, table.id, token, role)

            response.onSuccess {
                _ui.update{
                    it.copy(
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Change Role Failed."
                    )
                }
            }
        }

    }
    fun leaveTable(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InTable) return null

        val user = session.user
        val table = session.table
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.leaveTable(user.id, table.id, token)

            response.onSuccess {
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InLobby(session.user, emptyList())
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Leave Table Failed."
                    )
                }
            }
        }
    }

    // Game-related operations

    fun createGame(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InTable) return null

        val user = session.user
        val table = session.table
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.createGame(table.id, user.id, token)

            response.onSuccess { game ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = SessionState.InGame(session.user, game)
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Create Game Failed."
                    )
                }
            }
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
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.placeOnBoard(user.id, game.id, token, gameState.card, gameState.idx, pos)

            response.onSuccess { newGame ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = session.copy(game = newGame),
                        gameUI = it.gameUI.copy(state = GameUIState.Idle)
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Place Card Failed."
                    )
                }
            }
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
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.rotateTile(user.id, game.id, token, gameState.idx, right)

            response.onSuccess { newGame ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = session.copy(game = newGame),
                        gameUI = it.gameUI.copy(
                            state = gameState.copy(card = TileCard((gameState.card as TileCard).tile.rotate(right)))
                        )
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Rotate Tile Failed."
                    )
                }
            }
        }

    }
    fun nextPhase(): Job? {

        val session = ui.value.session
        if(session !is SessionState.InGame) return null

        val user = session.user
        val game = session.game
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.nextPhase(user.id, game.id, token)

            response.onSuccess { newGame ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = null,
                        session = session.copy(game = newGame),
                        gameUI = it.gameUI.copy(state = GameUIState.Idle, movementUsed = 0)
                    )
                }
            }
            response.onFailure { error ->
                _ui.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = error.message ?: "Couldn't skip phase."
                    )
                }
            }

        }
    }
    fun selectCard(idx: UInt, card: Card): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val selected = gameUI.state is GameUIState.SelectCard && gameUI.state.idx == idx

        return viewModelScope.launch{
            _ui.update {
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
            _ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = GameUIState.PlacingCard(gameUI.state.idx, gameUI.state.card)
                    )
                )
            }
        }

    }
    fun inspectCard(card: Card?): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val previous = (gameUI.state as? GameUIState.InspectCard)?.previous
        val nextState =
            if(card == null) {
                if (previous is GameUIState.InspectPlayer) previous
                else GameUIState.Idle
            } else GameUIState.InspectCard(card, gameUI.state)

        return viewModelScope.launch{
            _ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state = nextState)
                )
            }
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
            _ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(boardZoom = newZoom)
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
            _ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(state =
                        if(selected) GameUIState.Idle
                        else GameUIState.InspectPlayer(player))
                )
            }
        }
    }
    fun selectBoardCharacter(tile: BoardTile): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val nextState = if(gameUI.state is GameUIState.SelectBoardCharacter || tile.character == null) GameUIState.Idle
                        else GameUIState.SelectBoardCharacter(tile)

        return viewModelScope.launch{
            _ui.update{
                it.copy(gameUI = gameUI.copy(state = nextState))
            }
        }
    }
    fun moveSignal(): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.SelectBoardCharacter) return null

        return viewModelScope.launch {
            _ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = GameUIState.MovingCharacter(
                            from = gameUI.state.position,
                            path = emptyList()
                        )
                    )
                )
            }
        }
    }
    fun moveCharacter(to: BoardTile): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.MovingCharacter) return null

        if(gameUI.state.path.lastOrNull() == to) {

            val user = session.user
            val game = session.game
            val token = user.auth?.token ?: return null.also {
                _ui.update { it.copy(errorMessage = "No token found.") }
            }

            return viewModelScope.launch{

                val response = clientApi.moveCharacter(
                    user.id,
                    game.id,
                    token,
                    gameUI.state.path.first(),
                    gameUI.state.path.last(),
                )

                response.onSuccess { newGame ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = null,
                            session = session.copy(game = newGame),
                            gameUI = it.gameUI.copy(
                                state = GameUIState.Idle,
                                movementUsed = gameUI.movementUsed + gameUI.state.path.size - 1
                            )
                        )
                    }
                }
                response.onFailure { error ->
                    _ui.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Couldn't move character."
                        )
                    }
                }

            }
        }

        val characterSpe = gameUI.state.from.character?.adjustStats()?.spe ?: 0
        val remainingMovement = characterSpe - gameUI.movementUsed
        if(remainingMovement <= 0) return null.also {
            _ui.update { it.copy(errorMessage = "This character cannot move this turn anymore.") }
        }

        val path = session.game.board.findPath(
            from = gameUI.state.from,
            to = to,
            maxDistance = remainingMovement
        )

        return viewModelScope.launch {
            _ui.update {
                it.copy(
                    errorMessage = null,
                    gameUI = gameUI.copy(
                        state = gameUI.state.copy(path = path)
                    )
                )
            }
        }
    }
}