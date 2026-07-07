package isel.pt.cbdcg.views.startMenu.tutorial

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import isel.pt.cbdcg.domain.Email
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Password
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.BattleBet
import isel.pt.cbdcg.domain.game.BattlePhase
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.Turn
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.addToHand
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.findPath
import isel.pt.cbdcg.domain.game.board.tile.AllTileEffects
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.rotate
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.hold
import isel.pt.cbdcg.domain.game.incrementModifiers
import isel.pt.cbdcg.domain.game.removeFromHand
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.error.BattleError
import kotlin.math.min
import kotlin.time.Clock

enum class TutorialOptions(val imageName: String) {
    Zoom("ENSW"),
    Inspect_Card("trainee"),
    Inspect_Board_Tile("Chest"),
    Place_Tile("EN"),
    Place_Character("alchemist"),
    Switch_Character("apprentice"),
    Equip_Item("machete"),
    Turn_Phases("TurnPhases"),
    Move_Character("thief"),
    Activate_Tile_Effect("BigChest"),
    Challenge("ninja"),
    Sneak("vagabond"),
    Attack_In_Battle("battle_attack"),
    Hold_In_Battle("battle_hold"),
    Flee_In_Battle("battle_flee"),
}
data class TutorialContext(
    val game: Game,
    val zoom: Float = 1f,
    val movementUsed: Int = 0,
    val path: List<BoardTile> = emptyList(),
)

@Composable
fun TutorialScreen(
    mainMenuNav: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
){

    var selectedTutorial by remember { mutableStateOf<TutorialOptions?>(null) }
    var inTutorial by remember { mutableStateOf(false) }

    if(!inTutorial){
        ChooseTutorial(
            mainMenuNav = mainMenuNav,
            getDrawable = getDrawable,
            selectedTutorial = selectedTutorial,
            selectTutorial = { if (selectedTutorial != it) selectedTutorial = it else inTutorial = true }
        )
    } else {

        val user1 = User(0u, Name("Tutorial User 1"), Email("user1@tutorial.cbdcg"), Password("TuToRiAl"))
        val user2 = User(1u, Name("Tutorial User 2"), Email("user2@tutorial.cbdcg"), Password("TuToRiAl"))
        val players = listOf(user1, user2).map{ user ->
            Player(
                user = user,
                hand = emptyMap(),
                currentCharacter = null
            )
        }
        val mockGame = Game(
            id = 0u,
            players = players,
            spectators = emptyList(),
            board = Board().copy(tiles = Board().tiles + mapOf(
                BoardPosition(0,1) to Tile(listOf(Direction.NORTH, Direction.SOUTH)),
                BoardPosition(0, 2) to Tile(listOf(Direction.EAST, Direction.SOUTH, Direction.WEST)),
                BoardPosition(1,2) to Tile(listOf(Direction.WEST, Direction.SOUTH)),
                BoardPosition(1, 1) to Tile(listOf(Direction.NORTH, Direction.SOUTH, Direction.WEST)),
                BoardPosition(0, -1) to Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)),
                BoardPosition(1, -1) to Tile(listOf(Direction.NORTH, Direction.WEST)),
                BoardPosition(1, 0) to Tile(listOf(Direction.SOUTH, Direction.WEST)),
                BoardPosition(0, -2) to Tile(listOf(Direction.NORTH, Direction.WEST)),
                BoardPosition(-1, -2) to Tile(listOf(Direction.EAST, Direction.WEST)),
                BoardPosition(-2, -2) to Tile(listOf(Direction.EAST, Direction.NORTH)),
                BoardPosition(-2, -1) to Tile(listOf(Direction.NORTH, Direction.SOUTH)),
                BoardPosition(-1, 2) to Tile(listOf(Direction.EAST, Direction.SOUTH)),
                BoardPosition(-1, 1) to Tile(Direction.entries),
                BoardPosition(-1, 0) to Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)),
                BoardPosition(-1, -1) to Tile(listOf(Direction.NORTH, Direction.WEST)),
                BoardPosition(-2, 0) to Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)),
                BoardPosition(-2, 1) to Tile(listOf(Direction.EAST, Direction.SOUTH))
            ).map{ BoardTile(it.key, it.value, null, null) }),
            tileDeck = emptyMap(),
            itemDeck = emptyMap(),
            turn = Turn(0u, listOf(0u,1u), TurnPhase.CONSTRUCTION, Clock.System.now().toEpochMilliseconds()),
            battle = null
        )
        val mockBattle = Battle(
            characters = listOf(
                PlayableCharacterCatalog.basicCharacters.drop(6).first(),
                PlayableCharacterCatalog.basicCharacters.drop(4).first()
            ),
            currentTurn = 0u,
            phase = BattlePhase.WAITING,
            itemBet = listOf(
                BattleBet(players[0], ItemCatalog.commonItems.first()),
                BattleBet(players[1], ItemCatalog.commonItems.drop(3).first()),
            ),
        )

        when(selectedTutorial){

            TutorialOptions.Zoom -> {

                var context by remember { mutableStateOf(TutorialContext(mockGame, 1f)) }

                ZoomTutorial(
                    context = context,
                    zoom = { amplify ->
                        context =
                            if (amplify) context.copy(zoom = (context.zoom + 0.25f).coerceAtMost(2f))
                            else context.copy(zoom = (context.zoom - 0.25f).coerceAtLeast(0.5f))
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null }
                )
            }

            TutorialOptions.Inspect_Card -> {

                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            players = mockGame.players.map {
                                val hand =
                                    it.addToHand(
                                        TileCard(
                                            Tile(
                                                listOf(Direction.NORTH, Direction.SOUTH),
                                                AllTileEffects.allTileEffects.keys.first()
                                            )
                                        )
                                    )
                                val second = hand.addToHand(CharacterCard(PlayableCharacterCatalog.basicCharacters.first()))
                                second.addToHand(ItemCard(ItemCatalog.commonItems.first()))
                            }
                        )
                    )
                ) }

                InspectCardTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null }
                )
            }

            TutorialOptions.Inspect_Board_Tile -> {

                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles = mockGame.board.tiles.map{ boardTile ->
                                    if(boardTile.pos.x == 1 && boardTile.pos.y == 1)
                                        boardTile.copy(tile = boardTile.tile.copy(specialEffect = AllTileEffects.allTileEffects.keys.first()))
                                    else if(boardTile.pos.x == 0 && boardTile.pos.y == -1)
                                        boardTile.copy(tile = boardTile.tile.copy(specialEffect = AllTileEffects.allTileEffects.keys.last()))
                                    else if(boardTile.pos.x == 1 && boardTile.pos.y == 0)
                                        boardTile.copy(character = PlayableCharacterCatalog.rareCharacters.first())
                                    else if(boardTile.pos.x == 1 && boardTile.pos.y == -1)
                                        boardTile.copy(character = PlayableCharacterCatalog.basicCharacters.last())
                                    else boardTile
                                }
                            )
                        )
                    )
                ) }

                InspectBoardTileTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null }
                )
            }

            TutorialOptions.Place_Tile -> {

                val effect = AllTileEffects.allTileEffects.keys.first()
                val tile = Tile(listOf(Direction.EAST, Direction.NORTH), effect)

                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles = mockGame.board.tiles.map{ boardTile ->
                                    if(boardTile.pos.x == -2 && boardTile.pos.y == 1)
                                        boardTile.copy(tile = Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)))
                                    else boardTile
                                }
                            ),
                            players = mockGame.players.map{
                                it.addToHand(TileCard(tile))
                            }
                        )
                    )
                ) }

                PlaceTileTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    rotate = { clockwise ->
                        val current = (context.game.players.first().hand.values.first() as TileCard).tile
                        val rotated = current.rotate(clockwise)

                        context = context.copy(
                            game = context.game.copy(
                                players = context.game.players.map{
                                    it.removeFromHand(1u).addToHand(TileCard(rotated))
                                }
                            )
                        )
                    },
                    addToBoard = {

                        val tile = (context.game.players.first().hand.values.first() as TileCard).tile
                        context = context.copy(
                            game = context.game.copy(
                                board = context.game.board.copy(
                                    tiles = context.game.board.tiles + BoardTile( it, tile, 0u, null)
                                ),
                                players = context.game.players.map{ it.removeFromHand(1U) }
                            )
                        )
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Turn_Phases -> {

                val tile = Tile(listOf(Direction.EAST, Direction.SOUTH))
                val character = PlayableCharacterCatalog.basicCharacters.first()
                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles = mockGame.board.tiles.map{ boardTile ->
                                    if(boardTile.pos.x == -2 && boardTile.pos.y == 1)
                                        boardTile.copy(tile = Tile(listOf(Direction.EAST, Direction.NORTH, Direction.SOUTH)))
                                    else boardTile
                                }
                            ),
                            players = mockGame.players.map{ player ->
                                player
                                    .addToHand(TileCard(tile))
                                    .addToHand(CharacterCard(character))
                            }
                        )
                    )
                ) }

                TurnPhasesTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    addToBoard = { pos ->
                        val selectedTile = context.game.board.tiles.find{ it.pos.equals(pos) }
                        val newBoard =
                            if(selectedTile == null) context.game.board.tiles + BoardTile(pos, tile, null, null)
                            else context.game.board.tiles.map{ boardTile ->
                                if(boardTile.pos.equals(pos)) boardTile.copy(character = character)
                                else boardTile
                            }

                        context = context.copy(
                            game = context.game.copy(
                                board = context.game.board.copy(tiles = newBoard),
                                players = context.game.players.map{
                                    if(it.hand.size == 2 && selectedTile == null) it.removeFromHand(1u)
                                    else if(it.hand.size == 2 && selectedTile != null) it.removeFromHand(2u)
                                    else it.removeFromHand(0u)
                                }
                            )
                        )
                    },
                    nextPhase = {
                        val nextPhase = when(context.game.turn.phase){
                            TurnPhase.CONSTRUCTION -> TurnPhase.SUBSTITUTION
                            TurnPhase.SUBSTITUTION -> TurnPhase.MOVEMENT
                            TurnPhase.MOVEMENT -> TurnPhase.CONSTRUCTION
                        }

                        context = context.copy(
                            game = context.game.copy(
                                turn = context.game.turn.copy(phase = nextPhase)
                            )
                        )
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Place_Character -> {

                val character = PlayableCharacterCatalog.basicCharacters.first()
                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            players = mockGame.players.map{ player ->
                                player.addToHand(CharacterCard(character))
                            }
                        )
                    )
                ) }

                PlaceCharacterTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    addToBoard = { pos ->
                        context = context.copy(
                            game = context.game.copy(
                                board = context.game.board.copy(
                                    tiles = context.game.board.tiles.map{
                                        if(it.pos.equals(pos)) it.copy(character = character)
                                        else it
                                    }
                                ),
                                players = context.game.players.map{ it.removeFromHand(1U) }
                            )
                        )
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Switch_Character -> {

                val character = PlayableCharacterCatalog.basicCharacters.drop(1).first()
                val boardCharacter = PlayableCharacterCatalog.basicCharacters.first()
                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles =
                                    mockGame.board.tiles.map{
                                        if(it.pos.equals(BoardPosition(0,1))) it.copy(character = boardCharacter)
                                        else it
                                    }
                            ),
                            players = mockGame.players.map{ player ->
                                player.addToHand(CharacterCard(character))
                            }
                        )
                    )
                ) }

                SwitchCharacterTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    addToBoard = { pos ->
                        context = context.copy(
                            game = context.game.copy(
                                board = context.game.board.copy(
                                    tiles = context.game.board.tiles.map{
                                        if(it.pos.equals(pos)) it.copy(character = character)
                                        else it
                                    }
                                ),
                                players = context.game.players.map{ it.removeFromHand(1u).addToHand(boardCharacter.toCard()) }
                            )
                        )
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Equip_Item -> {

                val character = PlayableCharacterCatalog.basicCharacters.first()
                val item = ItemCatalog.commonItems.first()
                var context by remember { mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles =
                                    mockGame.board.tiles.map{
                                        if(it.pos.equals(BoardPosition(0,1))) it.copy(character = character)
                                        else it
                                    }
                            ),
                            players = mockGame.players.map{ player ->
                                player.addToHand(ItemCard(item))
                            }
                        )
                    )
                ) }

                EquipItemTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    addToBoard = { pos ->
                        context = context.copy(
                            game = context.game.copy(
                                board = context.game.board.copy(
                                    tiles = context.game.board.tiles.map{
                                        if(it.pos.equals(pos)) it.copy(character = character.copy(items = listOf(item)))
                                        else it
                                    }
                                ),
                                players = context.game.players.map{ it.removeFromHand(1U) }
                            )
                        )
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Move_Character -> {

                val character = PlayableCharacterCatalog.basicCharacters.first()
                var context by remember{ mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles =
                                    mockGame.board.tiles.map{
                                        if(it.pos.equals(BoardPosition(0,1))) it.copy(character = character)
                                        else it
                                    }
                            )
                        )
                    )
                ) }

                val characterTile = requireNotNull(context.game.board.tiles.find{ it.character?.name == character.name })

                MovementTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    characterTile = characterTile,
                    path = context.path,
                    createPath = { pos ->
                        val dest = requireNotNull(context.game.board.tiles.find{ it.pos == pos })
                        val max = requireNotNull(characterTile.character).adjustStats().spe - context.movementUsed
                        context = context.createPath(characterTile, dest, max)
                    },
                    applyMovement = {
                        val currentCharacter = context.game.board.tiles.find{ it.character?.name == character.name }?.character
                        context = context.applyMovement(requireNotNull(currentCharacter))
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Activate_Tile_Effect -> {

                val character = PlayableCharacterCatalog.basicCharacters.first()
                var context by remember{ mutableStateOf(
                    value = TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles =
                                    mockGame.board.tiles.map{
                                        if(it.pos.equals(BoardPosition(0,1))) it.copy(character = character)
                                        else if(it.pos.equals(BoardPosition(1,2)))
                                            it.copy(tile = it.tile.copy(specialEffect = AllTileEffects.allTileEffects.keys.drop(1).first()))
                                        else it
                                    }
                            )
                        )
                    )
                ) }

                val characterTile = requireNotNull(context.game.board.tiles.find{ it.character?.name == character.name })

                ActivateTileEffectTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    characterTile = characterTile,
                    path = context.path,
                    createPath = { pos ->
                        val dest = requireNotNull(context.game.board.tiles.find{ it.pos == pos })
                        val max = requireNotNull(characterTile.character).adjustStats().spe - context.movementUsed
                        context = context.createPath(characterTile, dest, max)
                    },
                    applyMovement = {
                        val currentCharacter = context.game.board.tiles.find{ it.character?.name == character.name }?.character
                        context = context.applyMovement(requireNotNull(currentCharacter))
                    },
                    activateTile = {
                        context = context.copy(
                            game = context.game.copy(
                                players = context.game.players.map{
                                    it.addToHand(ItemCard(ItemCatalog.commonItems.drop(3).first()))
                                      .addToHand(ItemCard(ItemCatalog.specialItems.first()))
                                },
                                board = context.game.board.copy(
                                    tiles = context.game.board.tiles.map{
                                        if(it.pos.equals(BoardPosition(1,2))) it.copy(cooldown = 3u)
                                        else it
                                    }
                                )
                            )
                        )
                    },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Challenge -> {

                val attacker = PlayableCharacterCatalog.basicCharacters.first()
                val defender = PlayableCharacterCatalog.basicCharacters.drop(6).first()

                var context by remember { mutableStateOf(
                    TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles = mockGame.board.tiles.map{
                                    if(it.pos.equals(BoardPosition(1,1))) it.copy(character = attacker)
                                    else if(it.pos.equals(BoardPosition(1,2))) it.copy(character = defender)
                                    else it
                                }
                            )
                        )
                    )
                ) }

                ChallengeTutorial(
                    context = context,
                    zoom = {amplify -> context = context.zoom(amplify) },
                    attacker = attacker,
                    defender = defender,
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Sneak -> {

                val attacker = PlayableCharacterCatalog.rareCharacters.dropLast(1).last()
                val defender = PlayableCharacterCatalog.basicCharacters.drop(6).first()

                var context by remember { mutableStateOf(
                    TutorialContext(
                        game = mockGame.copy(
                            board = mockGame.board.copy(
                                tiles = mockGame.board.tiles.map{
                                    if(it.pos.equals(BoardPosition(1,1))) it.copy(character = attacker)
                                    else if(it.pos.equals(BoardPosition(1,2))) it.copy(character = defender)
                                    else it
                                }
                            )
                        )
                    )
                ) }

                val sneakTile = requireNotNull(context.game.board.tiles.find{ it.pos.equals(BoardPosition(0,2))})

                SneakTutorial(
                    context = context,
                    zoom = { amplify -> context = context.zoom(amplify) },
                    attacker = attacker,
                    defender = defender,
                    applyMovement = { context = context.copy(path = listOf(sneakTile)).applyMovement(attacker) },
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Attack_In_Battle -> {

                var context by remember { mutableStateOf(
                    TutorialContext(
                        game = mockGame.copy(
                            battle = mockBattle.copy(phase = BattlePhase.BATTLING, currentTurn = 1u)
                        ),
                    )
                ) }

                val battle = requireNotNull(context.game.battle)
                val attacker = battle.characters[0]
                val defender = battle.characters[1]

                val actions = listOf(
                    BattleAction(attacker, defender, PossibleBattleActions.ATTACK, Stats(), 0),
                    BattleAction(defender, null, PossibleBattleActions.FLEE, Stats(), 0)
                )

                AttackInBattleTutorial(
                    battle = battle,
                    characterName = battle.characters.first().name,
                    onClick = { context = context.addBattleActions(actions).resolveTurn()},
                    winner = battle.characters.count{ it.adjustStats().hp > 0 } <= 1,
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Hold_In_Battle -> {

                var context by remember { mutableStateOf(
                    TutorialContext(
                        game = mockGame.copy(
                            battle = mockBattle.copy(phase = BattlePhase.BATTLING, currentTurn = 1u)
                        ),
                    )
                ) }

                val battle = requireNotNull(context.game.battle)
                val attacker = battle.characters[0]
                val defender = battle.characters[1]

                val holding = listOf(
                    BattleAction(attacker, null, PossibleBattleActions.HOLD, Stats(), 0),
                    BattleAction(defender, attacker, PossibleBattleActions.ATTACK, Stats(), 0)
                )

                val firstAttack = listOf(
                    BattleAction(attacker, defender, PossibleBattleActions.ATTACK, Stats(), 0),
                    BattleAction(defender, null, PossibleBattleActions.HOLD, Stats(), 0)
                )

                val attacking = listOf(
                    BattleAction(attacker, defender, PossibleBattleActions.ATTACK, Stats(), 0),
                    BattleAction(defender, null, PossibleBattleActions.FLEE, Stats(), 0)
                )

                HoldInBattleTutorial(
                    battle = battle,
                    characterName = battle.characters.first().name,
                    onClick = {
                        context =
                            when (battle.currentTurn) {
                                1u -> context.addBattleActions(holding).resolveTurn()
                                2u -> context.addBattleActions(firstAttack).resolveTurn()
                                else -> context.addBattleActions(attacking).resolveTurn()
                            }
                    },
                    winner = battle.characters.count{ it.adjustStats().hp > 0 } <= 1,
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )
            }

            TutorialOptions.Flee_In_Battle -> {

                var context by remember { mutableStateOf(
                    TutorialContext(
                        game = mockGame.copy(
                            battle = mockBattle.copy(phase = BattlePhase.BATTLING, currentTurn = 1u)
                        ),
                    )
                ) }

                val battle = requireNotNull(context.game.battle)
                val attacker = battle.characters[0]
                val defender = battle.characters[1]

                val holding = listOf(
                    BattleAction(attacker, null, PossibleBattleActions.HOLD, Stats(), 0),
                    BattleAction(defender, null, PossibleBattleActions.FLEE, Stats(), 0)
                )

                val fleeing = listOf(
                    BattleAction(attacker, null, PossibleBattleActions.FLEE, Stats(), 0),
                    BattleAction(defender, attacker, PossibleBattleActions.ATTACK, Stats(), 0)
                )

                FleeInBattleTutorial(
                    battle = battle,
                    characterName = battle.characters.first().name,
                    onClick = {
                        context =
                            if (battle.currentTurn == 1u) context.addBattleActions(holding).resolveTurn()
                            else context.addBattleActions(fleeing).resolveTurn()
                    },
                    winner = battle.characters.count{ it.adjustStats().hp > 0 } <= 1,
                    getDrawable = getDrawable,
                    endTutorial = { inTutorial = false; selectedTutorial = null },
                )

            }
            null -> { inTutorial = false }
        }
    }
}

private fun TutorialContext.applyMovement(character: Character): TutorialContext =
    copy(
        game = game.copy(
            board = game.board.copy(
                tiles = game.board.tiles.map{
                    if(it.character?.name == character.name) it.copy(character = null)
                    else if(it.pos.equals(path.last().pos)) it.copy(character = character)
                    else it
                }
            )
        ),
        path = emptyList(),
        movementUsed = movementUsed + (path.size - 1)
    )

private fun TutorialContext.createPath(from: BoardTile, to: BoardTile, max: Int): TutorialContext =
    copy(
        path = game.board.findPath(
            from = from,
            to = to,
            maxDistance = max,
            ignoreCharacters = emptyList()
        )
    )

private fun TutorialContext.zoom(amplify: Boolean): TutorialContext =
    if(amplify) copy(zoom = (zoom + 0.25f).coerceAtMost(2f))
    else copy(zoom = (zoom - 0.25f).coerceAtLeast(0.5f))

private fun TutorialContext.addBattleActions(actions: List<BattleAction>): TutorialContext =
    copy(
        game = game.copy(
            battle = requireNotNull(game.battle).copy(
                pending = actions.map{ it.copy(turn = requireNotNull(game.battle).currentTurn.toInt()) }
            )
        )
    )
private fun TutorialContext.resolveTurn(): TutorialContext {

    val battle = requireNotNull(game.battle)
    val player = battle.characters.first()

    val updatedBattle = battle.pending.fold(battle.incrementModifiers()) { currentBattle, battleAction ->
        val character = currentBattle.characters.find{ it.name == battleAction.origin.name }

        if(character != null && character.adjustStats().hp > 0){
            when (battleAction.action) {
                PossibleBattleActions.HOLD -> currentBattle.hold(battleAction)
                PossibleBattleActions.FLEE -> currentBattle.simpleFlee(
                    battleAction,
                    battleAction.origin.name == player.name
                )

                PossibleBattleActions.ATTACK -> {
                    val target = currentBattle.characters.find{ it.name == battleAction.target?.name }
                    if(target != null && target.adjustStats().hp > 0) currentBattle.simpleAttack(battleAction)
                    else currentBattle
                }
                else -> currentBattle
            }
        } else currentBattle
    }

    val winner = updatedBattle.characters.filter{ it.adjustStats().hp > 0 }

    return  if(winner.size == 1) {
                copy(game =
                    game.copy(
                        battle = updatedBattle.copy(
                            pending = emptyList(),
                            phase = BattlePhase.ENDING
                        )
                    )
                )
            } else {
                copy(game =
                    game.copy(
                        battle = updatedBattle.copy(
                            pending = emptyList(), currentTurn = battle.currentTurn + 1u
                        )
                    )
                )
            }
}
private fun Battle.simpleFlee(action: BattleAction, flee: Boolean): Battle{

    val origin = characters.find { it.name == action.origin.name }
        ?: throw BattleError.CharacterNotFound(action.origin.name)

    val stats =
        if(!flee) Stats()
        else Stats(-99,0,0,0)

    val updatedCharacters = characters.map { character ->
        if (character.name == origin.name)
            character.addModifier(
                StatModifier(
                    stats = stats,
                    duration = 0u,
                    type = ModifierType.BATTLE_FLEE
                )
            )
        else character
    }

    return copy(
        characters = updatedCharacters,
        actions = actions + (currentTurn to ((actions[currentTurn] ?: emptyList()) + action.copy(stats = stats))),
        itemBet = if(!flee) itemBet
                  else itemBet.filter{ it.player.currentCharacter != origin.name }

    )
}

private fun Battle.simpleAttack(action: BattleAction): Battle{

    val origin = characters.find { it.name == action.origin.name }
        ?: throw BattleError.CharacterNotFound(action.origin.name)
    val originBattleStats = origin.adjustStats()

    val target = characters.find { it.name == action.target?.name }
        ?: throw BattleError.CharacterNotFound(action.target?.name ?: "")
    val targetBattleStats = target.adjustStats()

    val damage = originBattleStats.dmg - targetBattleStats.def
    val stats =
        Stats(
            hp = if(damage > 0) -min(targetBattleStats.hp, damage) else 0,
            dmg = 0,
            def = if(targetBattleStats.def <= 0) 0
                  else if(damage > 0) -target.adjustStats().def
                  else -originBattleStats.dmg,
            spe = 0
        )

    val updatedCharacters = characters.map { character ->
        if (character.name == target.name)
            character.addModifier(
                StatModifier(
                    stats = stats,
                    duration = 0u,
                    type = ModifierType.BATTLE_ATTACK
                )
            )
        else character
    }

    return copy(
        characters = updatedCharacters,
        actions = actions + (currentTurn to ((actions[currentTurn] ?: emptyList()) + action.copy(stats = stats)))
    )
}