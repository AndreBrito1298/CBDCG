package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.BattlePhase
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.tile.StatType
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.error.GameError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.repository.memory.GameRepositoryMem
import isel.pt.cbdcg.repository.memory.ParticipantRepositoryMem
import isel.pt.cbdcg.repository.memory.TableRepositoryMem
import isel.pt.cbdcg.repository.memory.UserRepositoryMem
import isel.pt.cbdcg.webapi.websocket.EventsPublisher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.runBlocking
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

private class FakeGameEventsPublisher : EventsPublisher {
    val lobbyEvents = mutableListOf<List<Table>>()
    val tableEvents = mutableListOf<Table>()
    val deletedTables = mutableListOf<Table>()
    val startedGames = mutableListOf<Game>()
    val gameEvents = mutableListOf<Game>()

    override suspend fun publishLobbyTables(tables: List<Table>) {
        lobbyEvents += tables
    }

    override suspend fun publishTableUpdated(table: Table) {
        tableEvents += table
    }

    override suspend fun publishGameStarted(table: Table, game: Game) {
        startedGames += game
    }

    override suspend fun publishTableDeleted(table: Table) {
        deletedTables += table
    }

    override suspend fun publishGameUpdated(game: Game) {
        gameEvents += game
    }

    fun clear() {
        lobbyEvents.clear()
        tableEvents.clear()
        deletedTables.clear()
        startedGames.clear()
        gameEvents.clear()
    }
}

class GameServiceTest {

    private val userRepo = UserRepositoryMem
    private val tableRepo = TableRepositoryMem
    private val participantRepo = ParticipantRepositoryMem
    private val gameRepo = GameRepositoryMem
    private val events = FakeGameEventsPublisher()
    private val userService = UserService(userRepo)
    private val tableService = TableService(userRepo, tableRepo, participantRepo, events)
    private lateinit var scope: CoroutineScope
    private lateinit var gameService: GameService

    @BeforeTest
    fun clearRepo() = runBlocking {
        scope = CoroutineScope(SupervisorJob())
        gameService = GameService(gameRepo, tableRepo, userRepo, events, scope)
        userRepo.clear()
        tableRepo.clear()
        participantRepo.clear()
        gameRepo.clear()
        events.clear()
    }

    @AfterTest
    fun cancelTimers() {
        scope.cancel()
    }

    private suspend fun createUser(id: Int = 1): User =
        userService.createUser(Name("user$id"), Email("user$id@example.com"), Password("password$id")).getOrThrow()

    private fun character(name: String, stats: Stats = Stats(5, 2, 1, 3)): PlayableCharacter =
        PlayableCharacter(name = name, baseStats = stats, grade = Grade.BASIC, evolution = null)

    private fun item(name: String = "iron_claw", grade: Grade = Grade.BASIC): Item =
        Item(name = name, stats = Stats(dmg = 1), grade = grade)

    private fun tile(
        connections: List<Direction> = Direction.entries,
        effect: TileEffect = TileEffect(),
    ): Tile =
        Tile(connections, effect)

    private fun boardTile(
        position: BoardPosition,
        tile: Tile = tile(),
        character: isel.pt.cbdcg.domain.game.character.Character? = null,
        cooldown: UInt = 0u,
    ): BoardTile =
        BoardTile(position, tile, cooldown, character)

    private suspend fun saveGameFor(game: Game): Game {
        gameRepo.save(game)
        return game
    }

    private suspend fun baseGame(
        user: User,
        player: Player = Player(user, emptyMap(), null),
        board: Board = Board(),
        tileDeck: Map<Tile, UInt> = emptyMap(),
        itemDeck: Map<Item, UInt> = emptyMap(),
        turn: Turn = Turn(1u, listOf(user.id), TurnPhase.MOVEMENT, Long.MAX_VALUE),
        battle: Battle? = null,
    ): Game =
        saveGameFor(
            Game(
                id = 0u,
                players = listOf(player),
                spectators = emptyList(),
                board = board,
                tileDeck = tileDeck,
                itemDeck = itemDeck,
                turn = turn,
                battle = battle,
            )
        )

    @Test
    fun `createGame creates players from ready participants and publishes lifecycle events`() = runBlocking {
        val owner = createUser(1)
        val guest = createUser(2)
        tableService.createTable(Name("tableOne"), owner.id).getOrThrow()
        tableService.joinTable(guest.id, 0u).getOrThrow()
        tableService.changeRole(owner.id, 0u, Role.READY).getOrThrow()
        tableService.changeRole(guest.id, 0u, Role.READY).getOrThrow()

        val game = gameService.createGame(tableId = 0u, userId = owner.id).getOrThrow()

        assertEquals(2, game.players.size)
        assertEquals(0u, game.id)
        assertNull(tableRepo.findById(0u))
        assertEquals(1, events.startedGames.size)
        assertTrue(events.gameEvents.isNotEmpty())
        assertTrue(events.lobbyEvents.last().isEmpty())
    }

    @Test
    fun `createGame rejects non owner`(): Unit = runBlocking {
        val owner = createUser(1)
        val guest = createUser(2)
        tableService.createTable(Name("tableOne"), owner.id).getOrThrow()

        assertFailsWith<TableError.OwnerOnly> {
            gameService.createGame(tableId = 0u, userId = guest.id).getOrThrow()
        }
    }

    @Test
    fun `rotateTile rotates tile cards in player hand`() = runBlocking {
        val user = createUser()
        val originalTile = tile(listOf(Direction.NORTH, Direction.EAST))
        val player = Player(user, mapOf(0u to TileCard(originalTile)), null)
        baseGame(user, player = player)

        val game = gameService.rotateTile(user.id, 0u, user.auth!!.token, idx = 0u, right = true).getOrThrow()
        val rotated = (game.players.single().hand.getValue(0u) as TileCard).tile

        assertEquals(listOf(Direction.EAST, Direction.SOUTH), rotated.connections)
    }

    @Test
    fun `placeOnBoard saves placed tile and publishes game update`() = runBlocking {
        val user = createUser()
        val placedTile = tile(listOf(Direction.SOUTH))
        val player = Player(user, mapOf(0u to TileCard(placedTile)), null)
        baseGame(
            user = user,
            player = player,
            board = Board(),
            turn = Turn(0u, listOf(user.id), TurnPhase.CONSTRUCTION, Long.MAX_VALUE),
        )

        val game = gameService.placeOnBoard(user.id, 0u, user.auth!!.token, placedTile.let(::TileCard), 0u, BoardPosition(0, 1)).getOrThrow()

        assertTrue(game.board.tiles.any { it.pos == BoardPosition(0, 1) && it.tile == placedTile })
        assertTrue(events.gameEvents.isNotEmpty())
    }

    @Test
    fun `nextPhase advances current game and publishes update`() = runBlocking {
        val user = createUser()
        val player = Player(user, emptyMap(), currentCharacter = "alchemist")
        baseGame(
            user = user,
            player = player,
            turn = Turn(1u, listOf(user.id), TurnPhase.CONSTRUCTION, Long.MAX_VALUE),
        )

        val game = gameService.nextPhase(user.id, 0u, user.auth!!.token).getOrThrow()

        assertEquals(TurnPhase.SUBSTITUTION, game.turn.phase)
        assertTrue(events.gameEvents.isNotEmpty())
    }

    @Test
    fun `leaveGame fails when requester is not a player`(): Unit = runBlocking {
        val user = createUser(1)
        val other = createUser(2)
        baseGame(user, player = Player(user, emptyMap(), null))

        assertFailsWith<GameError.PlayerNotFound> {
            gameService.leaveGame(other.id, 0u, other.auth!!.token).getOrThrow()
        }
    }

    @Test
    fun `CharacterMovement updater moves character through service`() = runBlocking {
        val user = createUser()
        val alchemist = character("alchemist")
        val origin = boardTile(BoardPosition(0, 0), character = alchemist)
        val target = boardTile(BoardPosition(1, 0))
        baseGame(
            user = user,
            player = Player(user, emptyMap(), currentCharacter = alchemist.name),
            board = Board(listOf(origin, target)),
        )

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "CharacterMovement", origin, listOf(target)).getOrThrow()

        assertNull(game.board.tiles.first { it.pos == origin.pos }.character)
        assertEquals(alchemist.name, game.board.tiles.first { it.pos == target.pos }.character?.name)
    }

    @Test
    fun `DrawItem updater draws item through service`() = runBlocking {
        val user = createUser()
        val item = item()
        val chest = boardTile(
            BoardPosition(0, 0),
            tile = tile(effect = TileEffect(TileEffectTypes.Chest, maxCooldown = 2u)),
        )
        val player = Player(user, emptyMap(), currentCharacter = "alchemist")
        baseGame(
            user = user,
            player = player,
            board = Board(listOf(chest)),
            itemDeck = mapOf(item to 1u),
        )

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "DrawItem", player, listOf(chest)).getOrThrow()

        assertEquals(2u, game.board.tiles.single().cooldown)
        assertTrue(game.players.single().hand.values.any { it == ItemCard(item) })
    }

    @Test
    fun `UpdateStatModifiers updater applies tile modifier through service`() = runBlocking {
        val user = createUser()
        val alchemist = character("alchemist")
        val effectTile = boardTile(
            BoardPosition(0, 0),
            tile = tile(effect = TileEffect(TileEffectTypes.StatUp(StatType.Dmg), maxCooldown = 2u)),
            character = alchemist,
        )
        val player = Player(user, emptyMap(), currentCharacter = alchemist.name)
        baseGame(user, player = player, board = Board(listOf(effectTile)))

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "UpdateStatModifiers", player, listOf(effectTile)).getOrThrow()

        val updatedCharacter = game.board.tiles.single().character as PlayableCharacter
        assertEquals(2u, game.board.tiles.single().cooldown)
        assertTrue(updatedCharacter.activeStatModifiers.isNotEmpty())
    }

    @Test
    fun `BattleStart updater starts battle through service`() = runBlocking {
        val user = createUser()
        val attacker = character("alchemist")
        val defender = character("guardian")
        val player = Player(user, emptyMap(), currentCharacter = attacker.name)
        baseGame(
            user = user,
            player = player,
            board = Board(
                listOf(
                    boardTile(BoardPosition(0, 0), character = attacker),
                    boardTile(BoardPosition(0, 1), character = defender),
                )
            ),
        )

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "BattleStart", attacker, listOf(defender)).getOrThrow()

        assertNotNull(game.battle)
        assertEquals(BattlePhase.BATTLING, game.battle?.phase)
    }

    @Test
    fun `JoinBattle updater adds support character through service`() = runBlocking {
        val user = createUser()
        val support = character("thief")
        val battle = Battle(
            characters = listOf(character("alchemist"), character("guardian"), support),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        baseGame(user, player = Player(user, emptyMap(), currentCharacter = support.name), battle = battle)

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "JoinBattle", support, emptyList()).getOrThrow()

        assertTrue(game.battle?.pending?.any { it.origin.name == support.name } == true)
    }

    @Test
    fun `AddActionToPending updater queues battle action through service`() = runBlocking {
        val user = createUser()
        val actor = character("alchemist")
        val action = BattleAction(actor, null, PossibleBattleActions.HOLD, Stats(), 1)
        val battle = Battle(
            characters = listOf(actor, character("guardian")),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        baseGame(user, battle = battle)

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "AddActionToPending", action, emptyList()).getOrThrow()

        assertEquals(listOf(action), game.battle?.pending)
    }

    @Test
    fun `LeaveBattle updater removes non main character through service`() = runBlocking {
        val user = createUser()
        val support = character("thief")
        val battle = Battle(
            characters = listOf(character("alchemist"), character("guardian"), support),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            itemBet = emptyList(),
        )
        baseGame(user, battle = battle)

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "LeaveBattle", support, emptyList()).getOrThrow()

        assertTrue(game.battle?.characters?.none { it.name == support.name } == true)
    }

    @Test
    fun `RemoveActionFromPending updater removes queued action through service`() = runBlocking {
        val user = createUser()
        val actor = character("alchemist")
        val action = BattleAction(actor, null, PossibleBattleActions.HOLD, Stats(), 1)
        val battle = Battle(
            characters = listOf(actor, character("guardian")),
            currentTurn = 1u,
            phase = BattlePhase.BATTLING,
            pending = listOf(action),
            itemBet = emptyList(),
        )
        baseGame(user, battle = battle)

        val game = gameService.applyGameUpdater(user.id, 0u, user.auth!!.token, "RemoveActionFromPending", actor, emptyList()).getOrThrow()

        assertTrue(game.battle?.pending?.isEmpty() == true)
    }
}
