package isel.pt.cbdcg.repository.db

import isel.pt.cbdcg.configs.dbInit
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.Spectator
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.repository.database.GameRepositoryDB
import isel.pt.cbdcg.repository.database.UserRepositoryDB
import kotlinx.coroutines.runBlocking
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GameRepositoryTest {

    private val gameRepo = GameRepositoryDB
    private val userRepo = UserRepositoryDB

    private lateinit var playerOneUser: User
    private lateinit var playerTwoUser: User
    private lateinit var spectatorUser: User

    private val straightTile = Tile(listOf(Direction.NORTH, Direction.SOUTH))
    private val cornerTile = Tile(listOf(Direction.NORTH, Direction.EAST))
    private val basicItem = ItemCatalog.specialItems.first()
    private val otherItem = ItemCatalog.commonItems[1]

    @BeforeTest
    fun resetDb() = runBlocking {
        dbInit(reset = true)
        playerOneUser = userRepo.createUser(Name("PlayerOne"), Email("player.one@email.com"), Password("secret1"))
        playerTwoUser = userRepo.createUser(Name("PlayerTwo"), Email("player.two@email.com"), Password("secret1"))
        spectatorUser = userRepo.createUser(Name("Spectator"), Email("spectator@email.com"), Password("secret1"))
    }

    private fun players(): List<Player> =
        listOf(
            Player(
                user = playerOneUser,
                hand = mapOf(0u to TileCard(straightTile), 1u to ItemCard(basicItem)),
                currentCharacter = null,
            ),
            Player(
                user = playerTwoUser,
                hand = mapOf(0u to TileCard(cornerTile), 1u to ItemCard(otherItem)),
                currentCharacter = null,
            ),
        )

    @Test
    fun `create game stores players spectators and initial turn`() = runBlocking {
        val players = players()
        val spectators = listOf(Spectator(spectatorUser))
        val turnOrder = players.map { it.user.id }
        val tileDeck = mapOf(straightTile to 4u, cornerTile to 2u)
        val itemDeck = mapOf(basicItem to 3u, otherItem to 1u)

        val created = gameRepo.createGame(players, spectators, turnOrder, tileDeck, itemDeck)
        val stored = gameRepo.findById(created.id)

        assertNotNull(stored)
        assertEquals(created.id, stored.id)
        assertEquals(players, stored.players)
        assertEquals(spectators, stored.spectators)
        assertEquals(0u, stored.turn.gameTurn)
        assertEquals(TurnPhase.CONSTRUCTION, stored.turn.phase)
        assertEquals(turnOrder.first(), stored.turn.playerTurn.first())
        assertEquals(tileDeck, stored.tileDeck)
        assertEquals(itemDeck, stored.itemDeck)
    }

    @Test
    fun `get all games returns all created games`() = runBlocking {
        val players = players()
        val spectators = listOf(Spectator(spectatorUser))
        val turnOrder = players.map { it.user.id }
        val tileDeck = mapOf(straightTile to 2u)
        val itemDeck = mapOf(basicItem to 2u)

        val first = gameRepo.createGame(players, spectators, turnOrder, tileDeck, itemDeck)
        val second = gameRepo.createGame(players.reversed(), emptyList(), turnOrder.reversed(), tileDeck, itemDeck)

        val games = gameRepo.getAllGames()

        assertEquals(2, games.size)
        assertTrue(games.any { it.id == first.id })
        assertTrue(games.any { it.id == second.id })
    }

    @Test
    fun `save updates persisted turn and decks`() = runBlocking {
        val players = players()
        val created = gameRepo.createGame(
            players = players,
            spectators = listOf(Spectator(spectatorUser)),
            turnOrder = players.map { it.user.id },
            startingDeck = mapOf(straightTile to 3u),
            itemDeck = mapOf(basicItem to 2u),
        )

        val updated = created.copy(
            tileDeck = mapOf(cornerTile to 9u),
            itemDeck = mapOf(otherItem to 7u),
            turn = Turn(
                gameTurn = 5u,
                playerTurn = listOf(playerTwoUser.id, playerOneUser.id),
                phase = TurnPhase.MOVEMENT,
            ),
        )

        gameRepo.save(updated)
        val stored = gameRepo.findById(created.id)

        assertNotNull(stored)
        assertEquals(5u, stored.turn.gameTurn)
        assertEquals(TurnPhase.MOVEMENT, stored.turn.phase)
        assertEquals(playerTwoUser.id, stored.turn.playerTurn.first())
        assertEquals(mapOf(cornerTile to 9u), stored.tileDeck)
        assertEquals(mapOf(otherItem to 7u), stored.itemDeck)
    }

    @Test
    fun `tile deck persistence preserves special effects`() = runBlocking {
        val players = players()
        val effectTile = straightTile.copy(
            specialEffect = TileEffect(
                type = TileEffectTypes.Chest,
                maxCooldown = 2u,
                info = "Draw one item.",
            )
        )

        val created = gameRepo.createGame(
            players = players,
            spectators = listOf(Spectator(spectatorUser)),
            turnOrder = players.map { it.user.id },
            startingDeck = mapOf(effectTile to 1u),
            itemDeck = mapOf(basicItem to 1u),
        )

        val stored = gameRepo.findById(created.id)

        assertNotNull(stored)
        assertEquals(mapOf(effectTile to 1u), stored.tileDeck)
    }

    @Test
    fun `delete by id removes game`() = runBlocking {
        val players = players()
        val created = gameRepo.createGame(
            players = players,
            spectators = emptyList(),
            turnOrder = players.map { it.user.id },
            startingDeck = mapOf(straightTile to 1u),
            itemDeck = mapOf(basicItem to 1u),
        )

        gameRepo.deleteById(created.id)

        assertNull(gameRepo.findById(created.id))
    }

    @Test
    fun `clear removes every game`() = runBlocking {
        val players = players()
        val turnOrder = players.map { it.user.id }

        gameRepo.createGame(players, emptyList(), turnOrder, mapOf(straightTile to 1u), mapOf(basicItem to 1u))
        gameRepo.createGame(players.reversed(), emptyList(), turnOrder.reversed(), mapOf(cornerTile to 1u), mapOf(otherItem to 1u))

        gameRepo.clear()

        assertEquals(emptyList(), gameRepo.getAllGames())
    }
}
