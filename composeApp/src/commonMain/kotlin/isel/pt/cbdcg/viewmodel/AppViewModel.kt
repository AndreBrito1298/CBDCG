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
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.findPath
import isel.pt.cbdcg.domain.game.board.rotate
import isel.pt.cbdcg.domain.game.character.adjustStats
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    val ui: StateFlow<AppUIState>
        field = MutableStateFlow(AppUIState())

    init {
        
        // Viewmodel must be ready to react to updates in the clientApi

        viewModelScope.launch {
            clientApi.tables.collect { tables ->
                ui.update { ui ->
                    val session = ui.session

                    if(session is SessionState.InLobby)
                        ui.copy(session= session.copy(tables = tables))
                    else ui
                }
            }
        }
        viewModelScope.launch {
            clientApi.currentTable.collect { table ->
                ui.update { ui ->
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
                ui.update { ui ->
                    when (val session = ui.session) {
                        is SessionState.InGame if game != null -> {

                            val nextUI = ui.copy(session = session.copy(game = game))

                            val winner = game.winner
                            if (winner != null) {
                                nextUI.copy(gameUI = nextUI.gameUI.copy(state = GameUIState.GameOver(winner)))
                            } else nextUI

                        }

                        is SessionState.InTable if game != null ->
                            ui.copy(session = SessionState.InGame(session.user, game))

                        else -> ui
                    }
                }
            }
        }
    }

    fun dismissError() {
        ui.update { it.copy(errorMessage = null, gameUI = it.gameUI.copy(state= GameUIState.Idle)) }
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
    fun inspectCard(card: Card?): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null

        val previous = (gameUI.state as? GameUIState.InspectCard)?.previous
        val nextState =
            when (card) {
                null -> previous as? GameUIState.InspectPlayer ?: GameUIState.Idle
                is TileCard -> GameUIState.InspectTileEffect(card.tile)
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

    // Vão ser adaptadas ao novo modelo. A estrutura já obedece às regras, basta concluir a implementação

    fun moveCharacter(to: BoardTile): Job? {

        val session = ui.value.session
        val gameUI = ui.value.gameUI
        if(session !is SessionState.InGame) return null
        if(gameUI.state !is GameUIState.MovingCharacter) return null

        if(gameUI.state.path.lastOrNull() == to) {

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
                    gameUI.state.path.first(),
                    gameUI.state.path.last(),
                ).fold(
                    onSuccess = { newGame ->
                        val character = newGame.players.firstOrNull { player -> player.user.id == user.id }?.currentCharacter
                        val boardTile = newGame.board.tiles.firstOrNull { boardTile ->
                            boardTile.character?.name == character
                        }
                        val specialEffect = boardTile?.tile?.specialEffect?.type
                        val winner = newGame.winner

                        val nextState =
                            if (winner != null) GameUIState.GameOver(winner)
                            else if (specialEffect != null && specialEffect != TileEffectTypes.None && specialEffect != TileEffectTypes.Start)
                                GameUIState.InspectTileEffect(boardTile.tile, boardTile)
                            else GameUIState.Idle

                        ui.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = null,
                                session = session.copy(game = newGame),
                                gameUI = it.gameUI.copy(
                                    state = nextState,
                                    movementUsed = gameUI.movementUsed + gameUI.state.path.size - 1
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

        val characterSpe = gameUI.state.from.character?.adjustStats()?.spe ?: 0
        val remainingMovement = characterSpe - gameUI.movementUsed
        if(remainingMovement <= 0) return null.also {
            ui.update { it.copy(errorMessage = "This character cannot move this turn anymore.") }
        }

        val path = session.game.board.findPath(
            from = gameUI.state.from,
            to = to,
            maxDistance = remainingMovement
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

        val boardTile = gameUI.state.activateInTile ?: return null.also{
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

        val boardTile = gameUI.state.activateInTile ?: return null.also{
            ui.update { it.copy(errorMessage = null, gameUI = gameUI.copy(state = GameUIState.Idle)) }
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
                            errorMessage = error.message ?: "Couldn't draw Item."
                        )
                    }
                }
            )
        }
    }
}