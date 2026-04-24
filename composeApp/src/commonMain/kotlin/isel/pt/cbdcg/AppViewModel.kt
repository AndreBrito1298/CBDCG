package isel.pt.cbdcg

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Game
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AppUIState(
    val user: User? = null,
    val tables: List<Table> = emptyList(),
    val game: Game? = null,
    val isLoading: Boolean = false,
    val currentTable: Table? = null,
    val errorMessage: String? = null
)


class AppViewModel(
    val clientApi: ClientApi,
): ViewModel() {

    private val _ui = MutableStateFlow<AppUIState>(AppUIState())
    val ui = _ui.asStateFlow()

    init {

        // Viewmodel must be ready to react to updates in the clientApi

        viewModelScope.launch {
            clientApi.tables.collect { tables ->
                _ui.update { it.copy(tables = tables) }
            }
        }

        viewModelScope.launch {
            clientApi.currentTable.collect { table ->
                _ui.update { it.copy(currentTable = table) }
            }
        }

        viewModelScope.launch {
            clientApi.game.collect { game ->
                _ui.update { it.copy(game = game) }
            }
        }
    }

    fun dismissError() {
        _ui.update { it.copy(errorMessage = null) }
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

    fun stopObserving(onSuccess: () -> Unit): Job =
        viewModelScope.launch {
            clientApi.disconnectAll()
            onSuccess()
        }

    // User-related operations

    fun login(email: String, password: String, onSuccess: () -> Unit): Job =
        viewModelScope.launch {

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val email = Email(email)
            val password = Password(password)

            val response = clientApi.login(email, password)

            response.onSuccess { user ->
                _ui.update { it.copy(user = user, isLoading = false, errorMessage = null) }
                onSuccess()
            }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Login Failed.") }
            }
        }

    fun logout(onSuccess: () -> Unit): Job? {

        val user = _ui.value.user ?: return null
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch {

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.logout(token)

            response.onSuccess {
                clientApi.disconnectAll()
                _ui.value = AppUIState()
                onSuccess()
            }
            response.onFailure { error ->
                _ui.update{ it.copy(isLoading = false, errorMessage = error.message ?: "Logout Failed.") }
            }
        }
    }

    fun createUser(name: String, email: String, password: String, onSuccess: () -> Unit): Job =
        viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val name = Name(name)
            val email = Email(email)
            val password = Password(password)

            val response = clientApi.createUser(name, email, password)

            response.onSuccess { user ->
                _ui.update { it.copy(user = user, isLoading = false, errorMessage = null) }
                onSuccess()
            }
            response.onFailure { error ->
                _ui.update{ it.copy(isLoading = false, errorMessage = error.message ?: "Create User Failed.") }
            }
        }

    // Table-related operations

    fun getTables(): Job? {

        val user = _ui.value.user ?: return null

        return viewModelScope.launch {

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.getTables()
            response.onSuccess { tables ->
                    _ui.update { it.copy(user = user, tables = tables, isLoading = false, errorMessage = null) }
                }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Could not load tables.") }
            }
        }
    }

    fun joinTable(table: Table, onSuccess: (Table) -> Unit): Job? {

        val user = _ui.value.user ?: return null
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.joinTable(user.id, table.id, token)
            response.onSuccess { table ->
                _ui.update { it.copy(currentTable = table, isLoading = false, errorMessage = null) }
                onSuccess(table)
            }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Join Table Failed.") }
            }
        }
    }

    fun createTable(tableName: String, onSuccess: (Table) -> Unit): Job? {

        val user = _ui.value.user ?: return null
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val name = Name(tableName)
            val id = user.id

            val response = clientApi.createTable(name, id, token)
            response.onSuccess { table ->
                _ui.update { it.copy(currentTable = table, isLoading = false, errorMessage = null) }
                onSuccess(table)
            }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Create Table Failed.") }
            }
        }
    }

    fun changeRole(): Job? {

        val user = _ui.value.user ?: return null
        val table = _ui.value.currentTable ?: return null
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.changeRole(user.id, table.id, token)
            response.onSuccess {
                _ui.update{ it.copy(isLoading = false, errorMessage = null) }
            }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Change Role Failed.") }
            }
        }

    }

    fun leaveTable(onSuccess: () -> Unit): Job? {

        val user = _ui.value.user ?: return null
        val table = _ui.value.currentTable ?: return null
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.leaveTable(user.id, table.id, token)
            response.onSuccess {
                _ui.update { it.copy(currentTable = null, isLoading = false, errorMessage = null) }
                onSuccess()
            }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Leave Table Failed.") }
            }
        }
    }

    // Game-related operations

    fun createGame(onSuccess: () -> Unit): Job? {

        val user = _ui.value.user ?: return null
        val token = user.auth?.token ?: return null.also {
            _ui.update { it.copy(errorMessage = "No token found.") }
        }

        val table = _ui.value.currentTable ?: return null

        return viewModelScope.launch{

            _ui.update { it.copy(isLoading = true, errorMessage = null) }

            val response = clientApi.createGame(user.id, table.id, token)
            response.onSuccess { game ->
                _ui.update { it.copy(game = game, isLoading = false, errorMessage = null) }
            }
            response.onFailure { error ->
                _ui.update { it.copy(isLoading = false, errorMessage = error.message ?: "Create Game Failed.") }
            }
        }
    }
}