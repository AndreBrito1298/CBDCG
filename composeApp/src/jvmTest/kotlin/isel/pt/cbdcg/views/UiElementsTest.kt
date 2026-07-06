package isel.pt.cbdcg.views

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.views.lobby.SearchTablesScreen
import isel.pt.cbdcg.views.lobby.WaitingTableScreen
import isel.pt.cbdcg.views.lobby.utils.CreateTableCard
import isel.pt.cbdcg.views.lobby.utils.TableCard
import isel.pt.cbdcg.views.startMenu.CreateUserScreen
import isel.pt.cbdcg.views.startMenu.LoginScreen
import isel.pt.cbdcg.views.startMenu.MenuScreen
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule

class UiElementsTest {

    @get:Rule
    val compose = createComposeRule()

    @Test
    fun menuScreenShowsMainActionsAndRunsNavigationCallbacks() {
        var loginClicks = 0
        var createUserClicks = 0

        compose.setThemedContent {
            MenuScreen(
                loginNav = { loginClicks++ },
                createUserNav = { createUserClicks++ },
            )
        }

        compose.onNodeWithText("Card-Based Dungeon Crawling Game").assertIsDisplayed()
        compose.clickButton("Log In").performClick()
        compose.clickButton("Create User").performClick()

        assertEquals(1, loginClicks)
        assertEquals(1, createUserClicks)
    }

    @Test
    fun loginScreenValidatesFieldsAndSubmitsTypedCredentials() {
        var backClicks = 0
        var submittedEmail: String? = null
        var submittedPassword: String? = null

        compose.setThemedContent {
            LoginScreen(
                mainMenuNav = { backClicks++ },
                login = { email, password ->
                    submittedEmail = email
                    submittedPassword = password
                },
            )
        }

        compose.clickButton("Back").performClick()
        assertEquals(1, backClicks)

        compose.clickButton("Log In").assertIsNotEnabled()

        compose.onNodeWithText("Email").performTextInput("not-an-email")
        compose.onNodeWithText("Password").performTextInput("1234")

        compose.onNodeWithText("Email format is invalid.").assertIsDisplayed()
        compose.onNodeWithText("Password must have at least 5 characters.").assertIsDisplayed()
        compose.clickButton("Log In").assertIsNotEnabled()

        compose.setThemedContent {
            LoginScreen(
                mainMenuNav = {},
                login = { email, password ->
                    submittedEmail = email
                    submittedPassword = password
                },
            )
        }

        compose.onNodeWithText("Email").performTextInput("alice@example.com")
        compose.onNodeWithText("Password").performTextInput("secret")

        compose.clickButton("Log In").assertIsEnabled().performClick()

        assertEquals("alice@example.com", submittedEmail)
        assertEquals("secret", submittedPassword)
    }

    @Test
    fun createUserScreenValidatesAllInputsAndSubmitsNewUser() {
        var submitted: Triple<String, String, String>? = null

        compose.setThemedContent {
            CreateUserScreen(
                mainMenuNav = {},
                create = { name, email, password -> submitted = Triple(name, email, password) },
            )
        }

        compose.clickButton("Create User").assertIsNotEnabled()

        compose.onNodeWithText("Name").performTextInput("A name that is far too long")
        compose.onNodeWithText("Email").performTextInput("invalid")
        compose.onNodeWithText("Password").performTextInput("1234")

        compose.onNodeWithText("Name cannot have more than 20 characters.").assertIsDisplayed()
        compose.onNodeWithText("Email format is invalid.").assertIsDisplayed()
        compose.onNodeWithText("Password must have at least 5 characters.").assertIsDisplayed()

        compose.setThemedContent {
            CreateUserScreen(
                mainMenuNav = {},
                create = { name, email, password -> submitted = Triple(name, email, password) },
            )
        }

        compose.onNodeWithText("Name").performTextInput("Alice")
        compose.onNodeWithText("Email").performTextInput("alice@example.com")
        compose.onNodeWithText("Password").performTextInput("secret")

        compose.clickButton("Create User").assertIsEnabled().performClick()

        assertEquals(Triple("Alice", "alice@example.com", "secret"), submitted)
    }

    @Test
    fun createTableCardDisablesInvalidNamesAndSubmitsValidName() {
        var createdName: String? = null

        compose.setThemedContent {
            CreateTableCard(createTable = { createdName = it })
        }

        compose.clickButton("Create Table").assertIsNotEnabled()

        compose.onNodeWithText("Name").performTextInput("A table name that is too long")
        compose.onNodeWithText("Name cannot have more than 20 characters.").assertIsDisplayed()
        compose.clickButton("Create Table").assertIsNotEnabled()

        compose.onNodeWithText("A table name that is too long").performTextClearance()
        compose.onNodeWithText("Name").performTextInput("Dungeon Run")
        compose.clickButton("Create Table").assertIsEnabled().performClick()

        assertEquals("Dungeon Run", createdName)
    }

    @Test
    fun tableCardShowsTableDetailsAndJoins() {
        var joined = false
        val owner = user(1u, "Owner")
        val table = table(
            name = "Alpha",
            owner = owner,
            participants = listOf(
                Participant(owner, Role.PLAYER),
                Participant(user(2u, "Guest"), Role.SPECTATOR),
            ),
        )

        compose.setThemedContent {
            TableCard(table = table, joinTable = { joined = true })
        }

        compose.onNodeWithText("Table: 'Alpha' | Owner: 'Owner[#1]'").assertIsDisplayed()
        compose.onNodeWithText("Number of Players: 2").assertIsDisplayed()
        compose.clickButton("Join").performClick()

        assertTrue(joined)
    }

    @Test
    fun searchTablesScreenListsTablesAndRunsLobbyCallbacks() {
        val currentUser = user(1u, "Alice")
        val alpha = table("Alpha", currentUser, listOf(Participant(currentUser, Role.PLAYER)))
        val beta = table("Beta", user(2u, "Bob"), emptyList())
        var joinedTable: Table? = null
        var createdTableName: String? = null
        var logoutClicks = 0

        compose.setThemedContent {
            SearchTablesScreen(
                user = currentUser,
                tables = listOf(alpha, beta),
                joinTable = { joinedTable = it },
                createTable = { createdTableName = it },
                logout = { logoutClicks++ },
            )
        }

        compose.onNodeWithText("Available Tables").assertIsDisplayed()
        compose.onNodeWithText("User: Alice").assertIsDisplayed()
        compose.onAllNodesWithText("Join").assertCountEquals(2)

        compose.clickButton("Logout").performClick()
        compose.clickButton("Join").performClick()
        compose.onNodeWithText("Name").performTextInput("New Table")
        compose.clickButton("Create Table").performClick()

        assertEquals(1, logoutClicks)
        assertEquals(alpha, joinedTable)
        assertEquals("New Table", createdTableName)
    }

    @Test
    fun waitingTableScreenLetsSpectatorBecomePlayerAndLeave() {
        val currentUser = user(1u, "Alice")
        val table = table(
            name = "Lobby",
            owner = currentUser,
            participants = listOf(
                Participant(currentUser, Role.SPECTATOR),
                Participant(user(2u, "Bob"), Role.READY),
            ),
        )
        var roleChange: Role? = null
        var leaveClicks = 0

        compose.setThemedContent {
            WaitingTableScreen(
                user = currentUser,
                table = table,
                changeRole = { roleChange = it },
                leaveTable = { leaveClicks++ },
                createGame = {},
            )
        }

        compose.onNodeWithText("Lobby").assertIsDisplayed()
        compose.onNodeWithText("User: Alice[#1]").assertIsDisplayed()
        compose.onNodeWithText("Players").assertIsDisplayed()
        compose.onNodeWithText("Spectators").assertIsDisplayed()
        compose.clickButton("Join Spectators").assertIsNotEnabled()
        compose.clickButton("Start Game").assertIsNotEnabled()

        compose.clickButton("Join Players").assertIsEnabled().performClick()
        compose.clickButton("Leave Table").performClick()

        assertEquals(Role.PLAYER, roleChange)
        assertEquals(1, leaveClicks)
    }

    @Test
    fun waitingTableScreenStartsGameWhenOwnerAndAllPlayersAreReady() {
        val owner = user(1u, "Alice")
        val table = table(
            name = "Ready Lobby",
            owner = owner,
            participants = listOf(
                Participant(owner, Role.READY),
                Participant(user(2u, "Bob"), Role.READY),
                Participant(user(3u, "Charlie"), Role.SPECTATOR),
            ),
        )
        var createGameClicks = 0
        var roleChange: Role? = null

        compose.setThemedContent {
            WaitingTableScreen(
                user = owner,
                table = table,
                changeRole = { roleChange = it },
                leaveTable = {},
                createGame = { createGameClicks++ },
            )
        }

        compose.clickButton("Join Players").assertIsNotEnabled()
        compose.clickButton("Join Spectators").assertIsEnabled()
        compose.clickButton("Start Game").assertIsEnabled().performClick()

        assertEquals(1, createGameClicks)
        assertEquals(null, roleChange)
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.setThemedContent(
        content: @Composable () -> Unit,
    ) {
        setContent {
            MaterialTheme {
                content()
            }
        }
    }

    private fun androidx.compose.ui.test.junit4.ComposeContentTestRule.clickButton(text: String) =
        onAllNodes(hasText(text) and hasClickAction())[0]

    private fun user(id: UInt, name: String) = User(
        id = id,
        name = Name(name),
        email = Email("${name.lowercase()}@example.com"),
        password = Password("secret"),
    )

    private fun table(
        name: String,
        owner: User,
        participants: List<Participant>,
    ) = Table(
        id = 1u,
        name = Name(name),
        owner = owner,
        participants = participants,
    )
}
