package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.TileEffect
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog.getCharacterByName
import isel.pt.cbdcg.domain.game.character.Stats

internal fun testUser(id: UInt): User =
    User(
        id = id,
        name = Name("user$id"),
        email = Email("user$id@example.com"),
        password = Password("password$id"),
    )

internal fun testCharacter(name: String, stats: Stats = Stats(3, 2, 1, 2)): PlayableCharacter =
    getCharacterByName(name)!!

internal fun testItem(name: String = "iron_claw", grade: Grade = Grade.BASIC): Item =
    Item(name = name, stats = Stats(dmg = 1), grade = grade)

internal fun testPlayer(
    id: UInt,
    hand: PlayerHand = emptyMap(),
    currentCharacter: String? = null,
): Player =
    Player(
        user = testUser(id),
        hand = hand,
        currentCharacter = currentCharacter,
    )

internal fun testTile(
    connections: List<Direction> = listOf(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST),
    effect: TileEffect = TileEffect(),
): Tile =
    Tile(connections = connections, specialEffect = effect)

internal fun testBoardTile(
    position: BoardPosition,
    tile: Tile = testTile(),
    character: isel.pt.cbdcg.domain.game.character.Character? = null,
    cooldown: UInt = 0u,
): BoardTile =
    BoardTile(position, tile, cooldown, character)

internal fun testBoardWith(vararg tiles: BoardTile): Board =
    Board(tiles = tiles.toList())

internal fun testGame(
    players: List<Player> = listOf(
        testPlayer(1u, currentCharacter = "alchemist"),
        testPlayer(2u, currentCharacter = "guardian"),
    ),
    board: Board = testBoardWith(
        testBoardTile(BoardPosition(0, 0), character = testCharacter("alchemist")),
        testBoardTile(BoardPosition(0, 1), character = testCharacter("guardian")),
    ),
    tileDeck: Deck<Tile> = emptyMap(),
    itemDeck: Deck<Item> = emptyMap(),
    turn: Turn = Turn(1u, players.map { it.user.id }, TurnPhase.CONSTRUCTION, 1_000L),
    battle: Battle? = null,
): Game =
    Game(
        id = 1u,
        players = players,
        spectators = emptyList(),
        board = board,
        tileDeck = tileDeck,
        itemDeck = itemDeck,
        turn = turn,
        battle = battle,
    )

internal fun chestTile(cooldown: UInt = 2u): Tile =
    testTile(effect = TileEffect(type = TileEffectTypes.Chest, maxCooldown = cooldown))

