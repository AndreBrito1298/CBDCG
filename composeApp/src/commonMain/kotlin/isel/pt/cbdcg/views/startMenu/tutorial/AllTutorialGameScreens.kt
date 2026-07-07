package isel.pt.cbdcg.views.startMenu.tutorial


import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Direction
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.views.game.utils.board.ZoomButtons
import isel.pt.cbdcg.views.game.utils.dialog.CardStatsDialog
import isel.pt.cbdcg.views.game.utils.dialog.CollisionOption
import isel.pt.cbdcg.views.game.utils.dialog.TileEffectDialog

@Composable
fun ZoomTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    var maxZoom by remember { mutableStateOf(false) }
    var minZoom by remember { mutableStateOf(false) }
    var returnedToDefault by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 85.dp,
            mainText = "Test the Board Zoom.",
            subText = "> Click the zoom Buttons until the Board is fully zoomed in and zoomed out.\nReturn to the Default Zoom (1.0).\nZoom: ${context.zoom}",
            canEnd = minZoom && maxZoom && returnedToDefault,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = { TutorialBoardTileState() },
            getDrawable = getDrawable,
            onClick = {}
        ) },
        buttons = { ZoomButtonsTutorial(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = {
                zoom(true)
                if(context.zoom + 0.25f >= 2f) maxZoom = true
                if(maxZoom && minZoom && context.zoom + 0.25f == 1f) returnedToDefault = true
            },
            reduce = {
                zoom(false)
                if(context.zoom - 0.25f <= 0.5f) minZoom = true
                if(maxZoom && minZoom && context.zoom - 0.25f == 1f) returnedToDefault = true
            },
            maxZoom = maxZoom,
            minZoom = minZoom
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { true },
            dropDownMenu = { CardDDM() }
        ) }
    )
}
@Composable
fun InspectCardTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    val player = context.game.players.first()
    var inspectCard by remember { mutableStateOf<Card?>(null) }
    val inspected = remember { mutableStateListOf<Card>() }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 55.dp,
            mainText = "Inspect cards to gather relevant information.",
            subText = "> Click each card on your hand and 'Inspect' them.",
            canEnd = inspected.size >= 3,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = { TutorialBoardTileState() },
            getDrawable = getDrawable,
            onClick = {  }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = player.hand,
            getDrawable = getDrawable,
            isLocked = { inspected.contains(it) },
            dropDownMenu = { CardDDM(inspectCard = true) },
            inspect = { inspectCard = it }
        ) }
    )

    when(val card = inspectCard){
        is CharacterCard,
        is ItemCard ->
            CardStatsDialog(
                getDrawable = getDrawable,
                card = card,
                unequip = {},
                onDismiss = { inspectCard = null; inspected += card }
            )
        is TileCard ->
            TileEffectDialog(
                getDrawable = getDrawable,
                effect = card.tile.specialEffect,
                activate = false,
                onConfirm = { inspectCard = null; inspected += card },
                affectedCharacters = emptyList(),
            )
        null -> {}
    }
}
@Composable
fun InspectBoardTileTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
) {

    var inspectBoardTile by remember { mutableStateOf<BoardTile?>(null) }
    val inspected = remember { mutableStateListOf<BoardPosition>() }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Inspect the Board Tiles with a Special Effect.",
            subText = "> Click both tiles with an Effect, and Inspect them.\n> Effects are represented by Icons on top of a Tile, except the Star.",
            canEnd = inspected.size >= 2,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            highlightTile = { inspected.contains(it.pos) },
            executableActions = { pos ->
                val equals = pos.equals(BoardPosition(1, 1)) || pos.equals(BoardPosition(0, -1))
                if(inspected.contains(pos) || !equals) TutorialBoardTileState()
                else TutorialBoardTileState(inspectTileEffect = true)
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                val boardTile = context.game.board.tiles.find{ it.pos.x == pos.x && it.pos.y == pos.y }
                inspectBoardTile = boardTile
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { true },
            dropDownMenu = { CardDDM() }
        ) }
    )

    when(val boardTile = inspectBoardTile){
        null -> {}
        else ->
            TileEffectDialog(
                getDrawable = getDrawable,
                effect = boardTile.tile.specialEffect,
                activate = false,
                onConfirm = {
                    if(!inspected.contains(boardTile.pos)) inspected += boardTile.pos
                    inspectBoardTile = null
                },
                affectedCharacters =
                    if(boardTile.tile.specialEffect.type == TileEffectTypes.Chest) emptyList()
                    else listOf(PlayableCharacterCatalog.rareCharacters.first(), PlayableCharacterCatalog.basicCharacters.last()),
            )
    }
}
@Composable
fun PlaceTileTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    addToBoard: (BoardPosition) -> Unit,
    rotate: (Boolean) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    var placingCard by remember { mutableStateOf(false) }
    var placed by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Place the Tile Card in a valid empty Board Position.",
            subText = "> Select a Tile Card in Hand.\n> Rotate the tile and place it on the only available space.",
            canEnd = placed,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = { pos ->
                TutorialBoardTileState(placeTile = placingCard && pos.equals(BoardPosition(-2, 2)))
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                addToBoard(pos)
                placed = true
                placingCard = false
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { false },
            dropDownMenu = { card ->
                if(placingCard) CardDDM()
                else {
                    val tile = (card as TileCard).tile
                    val south = tile.connections.contains(Direction.SOUTH)
                    val eastOrWest = tile.connections.contains(Direction.EAST) || tile.connections.contains(Direction.WEST)

                    if(south && eastOrWest) CardDDM(placeCard = true)
                    else CardDDM(rotateTile = true)
                }
            },
            rotate = { rotate(it) },
            place = { placingCard = true }
        ) }
    )
}

@Composable
fun PlaceCharacterTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    addToBoard: (BoardPosition) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    var placingCard by remember { mutableStateOf(false) }
    var placed by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Place the Character Card in any available Board Tile.",
            subText = "> Select a Character Card in Hand.\n> Place it on the board.",
            phase = TurnPhase.SUBSTITUTION,
            canEnd = placed,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = {
                TutorialBoardTileState(placeCharacter = placingCard)
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                addToBoard(pos)
                placed = true
                placingCard = false
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { false },
            dropDownMenu = {
                if(placingCard) CardDDM()
                else CardDDM(placeCard = true)
            },
            place = { placingCard = true }
        ) }
    )

}

@Composable
fun SwitchCharacterTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    addToBoard: (BoardPosition) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    var placingCard by remember { mutableStateOf(false) }
    var placed by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Switch characters.",
            subText = "> Select a Character Card in Hand.\n> Place it on top of your character that's is in the Board.",
            phase = TurnPhase.SUBSTITUTION,
            canEnd = placed,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = { pos ->
                TutorialBoardTileState(
                    placeCharacter = placingCard && pos.equals(BoardPosition(0,1)),
                    highlightCharacter = pos.equals(BoardPosition(0,1))
                )
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                addToBoard(pos)
                placed = true
                placingCard = false
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { false },
            dropDownMenu = {
                CardDDM(placeCard = !placingCard && !placed)
            },
            place = { placingCard = true }
        ) }
    )

}

@Composable
fun EquipItemTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    addToBoard: (BoardPosition) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){
    var placingCard by remember { mutableStateOf(false) }
    var placed by remember { mutableStateOf(false) }
    var inspectCharacter by remember { mutableStateOf<BoardTile?>(null) }
    var inspected by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Place the Item Card on top of your character.",
            subText = "> Select an Item Card in Hand.\n> Place it on the highlighted character.",
            phase = TurnPhase.SUBSTITUTION,
            canEnd = inspected,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            highlightTile = { false },
            executableActions = {
                TutorialBoardTileState(
                    placeItem = ! placed && placingCard && it.equals(BoardPosition(0,1)),
                    inspectCharacter = placed && it.equals(BoardPosition(0,1)),
                    highlightCharacter = it.equals(BoardPosition(0,1))
                )
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                if(!placed){
                    addToBoard(pos)
                    placed = true
                    placingCard = false
                } else {
                    val boardTile = context.game.board.tiles.find{ it.pos.x == pos.x && it.pos.y == pos.y }
                    inspectCharacter = boardTile
                }
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { false },
            dropDownMenu = {
                if(placingCard) CardDDM()
                else CardDDM(placeCard = true)
            },
            place = { placingCard = true }
        ) }
    )

    when(val boardTile = inspectCharacter){
        null -> {}
        else ->
            CardStatsDialog(
                getDrawable = getDrawable,
                card = boardTile.character.toCard(),
                unequip = {},
                onDismiss = { inspected = true; inspectCharacter = null }
            )
    }
}

@Composable
fun TurnPhasesTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    addToBoard: (BoardPosition) -> Unit,
    nextPhase: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    var placingTile by remember { mutableStateOf(false) }
    var tilePlaced by remember { mutableStateOf(false) }
    var placingCharacter by remember { mutableStateOf(false) }
    var characterPlaced by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 85.dp,
            mainText = "Learn what to do to step into the next Turn Phase.",
            subText = "> Construction Phase: Tile cards in hand (Current Max: 0)\n> Substitution Phase: characters on board (Current Min: 1).\nCurrent Phase: ${context.game.turn.phase}",
            phase = context.game.turn.phase,
            nextPhaseEnabled =
                (context.game.turn.phase == TurnPhase.CONSTRUCTION && tilePlaced) ||
                (context.game.turn.phase == TurnPhase.SUBSTITUTION && characterPlaced),
            nextPhase = nextPhase,
            canEnd = context.game.turn.phase == TurnPhase.MOVEMENT,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = { pos ->
                if(placingCharacter) TutorialBoardTileState(placeCharacter = true)
                else if(placingTile) TutorialBoardTileState(placeTile = pos.equals(BoardPosition(-2,2)))
                else TutorialBoardTileState()
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                addToBoard(pos)
                if(placingTile) {
                    placingTile = false
                    tilePlaced = true
                }
                if(placingCharacter) {
                    placingCharacter = false
                    characterPlaced = true
                }
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { false },
            dropDownMenu = { card ->
                if(placingTile || placingCharacter) CardDDM()
                else{
                    when (card) {
                        is TileCard -> CardDDM(placeCard = context.game.turn.phase == TurnPhase.CONSTRUCTION)
                        is CharacterCard -> CardDDM(placeCard = context.game.turn.phase == TurnPhase.SUBSTITUTION)
                        else -> CardDDM()
                    }
                }
            },
            place = { card ->
                when(card) {
                    is TileCard -> {
                        placingCharacter = false
                        placingTile = true
                    }
                    is CharacterCard -> {
                        placingTile = false
                        placingCharacter = true
                    }
                    else -> {}
                }
            }
        ) }
    )
}

@Composable
fun MovementTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    characterTile: BoardTile,
    path: List<BoardTile>,
    createPath: (BoardPosition) -> Unit,
    applyMovement: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    val character by remember { mutableStateOf(requireNotNull(characterTile.character)) }
    var moving by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 85.dp,
            mainText = "Move your character through the board.",
            subText = "> Number of movements = Character 'SPE' (Remaining Movements: ${character.adjustStats().spe - context.movementUsed}).\n> Click your character and select the 'Move' option.\n> Click on a Tile to preview the movement, and click it again to move.",
            phase = TurnPhase.MOVEMENT,
            canEnd = character.adjustStats().spe - context.movementUsed == 0,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            path = path,
            executableActions = { pos ->
                TutorialBoardTileState(
                    moveCharacter = !moving && character.adjustStats().spe - context.movementUsed > 0 && pos.equals(characterTile.pos),
                    applyMovement = moving,
                    highlightCharacter = pos.equals(characterTile.pos)
                )
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                if(!moving) moving = true
                else if(pos.equals(characterTile.pos)) moving = false
                else if(path.isEmpty() || path.last().pos != pos) createPath(pos)
                else { applyMovement(); moving = false }
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { true },
            dropDownMenu = { CardDDM() }
        ) }
    )
}

@Composable
fun ActivateTileEffectTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    characterTile: BoardTile,
    path: List<BoardTile>,
    createPath: (BoardPosition) -> Unit,
    applyMovement: () -> Unit,
    activateTile: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    var moving by remember { mutableStateOf(false) }
    var activated by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 85.dp,
            mainText = "Move on top of a Tile to activate its effect.",
            subText = "> Click your character and select the 'Move' option.\n> Move to the tile with the special effect and activate it's effect.",
            phase = TurnPhase.MOVEMENT,
            canEnd = activated,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            path = path,
            highlightTile = { it.pos == BoardPosition(1,2) },
            executableActions = { pos ->
                TutorialBoardTileState(
                    moveCharacter = !moving && !activated && pos.equals(characterTile.pos),
                    applyMovement = moving && pos.equals(BoardPosition(1,2)),
                    highlightCharacter = pos.equals(characterTile.pos)
                )
            },
            getDrawable = getDrawable,
            onClick = { pos ->
                if(!moving) moving = true
                else if(pos.equals(characterTile.pos)) moving = false
                else if(path.isEmpty() || path.last().pos != pos) createPath(pos)
                else { applyMovement(); moving = false }
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { true },
            dropDownMenu = { CardDDM() }
        ) }
    )

    if(characterTile.pos == BoardPosition(1,2) && !activated){
        TileEffectDialog(
            getDrawable = getDrawable,
            effect = characterTile.tile.specialEffect,
            activate = true,
            onConfirm = {
                activated = true
                activateTile()
            },
            affectedCharacters = listOf(requireNotNull(characterTile.character)),
        )
    }
}

@Composable
fun ChallengeTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    attacker: Character,
    defender: Character,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    val characterPos = requireNotNull(context.game.board.tiles.find{ it.character?.name == attacker.name }?.pos)
    val enemyPos = requireNotNull(context.game.board.tiles.find{ it.character?.name == defender.name }?.pos)
    var challenge by remember { mutableStateOf(false) }
    var challenged by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "When close, Characters can engage in battles.",
            subText = "> Click an enemy (not highlighted) and 'Challange' him.\n> Every adjacent character will be able to join the battle.",
            phase = TurnPhase.MOVEMENT,
            canEnd = challenged,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = {
                TutorialBoardTileState(
                    challenge = !challenge && it.equals(enemyPos),
                    highlightCharacter = it.equals(characterPos)
                )
            },
            getDrawable = getDrawable,
            onClick = {
                if(!challenge) challenge = true
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { true },
            dropDownMenu = { CardDDM() }
        ) }
    )

    if(challenge && !challenged){
        CharacterCollisionTutorialDialog(
            getDrawable = getDrawable,
            movingCharacter = attacker,
            staticCharacter = defender,
            canSneak = false,
            onClick = {
                when(it){
                    CollisionOption.COMBAT -> challenged = true
                    CollisionOption.SNEAK -> {  }
                    CollisionOption.CANCEL -> { challenge = false }
                }
            },
            onDismiss = {  }
        )
    }
}

@Composable
fun SneakTutorial(
    context: TutorialContext,
    zoom: (Boolean) -> Unit,
    attacker: Character,
    defender: Character,
    applyMovement: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    val characterPos = requireNotNull(context.game.board.tiles.find{ it.character?.name == attacker.name }?.pos)
    val enemyPos = requireNotNull(context.game.board.tiles.find{ it.character?.name == defender.name }?.pos)

    var sneak by remember { mutableStateOf(false) }
    var sneaked by remember { mutableStateOf(false) }
    var moved by remember { mutableStateOf(false) }

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 85.dp,
            mainText = "You cannot pass through characters..",
            subText = "> Click an enemy (not highlighted) and 'Challange' him.\n> If you have 2 or more Movement, you can try and sneak through.\n> Move past the enemy.",
            phase = TurnPhase.MOVEMENT,
            canEnd = moved,
            endTutorial = endTutorial
        ) },
        board = { BoardTutorial(
            zoom = context.zoom,
            board = context.game.board.tiles,
            executableActions = {
                TutorialBoardTileState(
                    challenge = !sneak && it.equals(enemyPos),
                    highlightCharacter = it.equals(characterPos),
                    sneakThrough = sneaked && !moved && it.equals(BoardPosition(0,2))
                )
            },
            getDrawable = getDrawable,
            onClick = {
                if(!sneak) sneak = true
                else if(sneaked && !moved) {
                    applyMovement()
                    moved = true
                }
            }
        ) },
        buttons = { ZoomButtons(
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
            amplify = { zoom(true) },
            reduce = { zoom(false) },
        ) },
        playerHand = { PlayerHandTutorial(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            hand = context.game.players.first().hand,
            getDrawable = getDrawable,
            isLocked = { true },
            dropDownMenu = { CardDDM() }
        ) }
    )

    if(sneak && !sneaked && !moved){
        CharacterCollisionTutorialDialog(
            getDrawable = getDrawable,
            movingCharacter = attacker,
            staticCharacter = defender,
            canSneak = true,
            onClick = {
                when(it){
                    CollisionOption.COMBAT -> {  }
                    CollisionOption.SNEAK -> { sneaked = true }
                    CollisionOption.CANCEL -> { sneak = false }
                }
            },
            onDismiss = {  }
        )
    }
}

@Composable
fun AttackInBattleTutorial(
    battle: Battle,
    characterName: String,
    onClick: () -> Unit,
    winner: Boolean,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){

    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Attacks will lower the oponents DEF and HP.",
            subText = "> Attack until you win the battle.\n> You can earn items from your opponents after winning a battle.",
            phase = TurnPhase.MOVEMENT,
            canEnd = winner,
            endTutorial = endTutorial
        ) },
        battle = { BattleTutorial(
            getDrawable = getDrawable,
            battleOptions = BattleOptions(attack = true),
            battle = battle,
            end = winner,
            characterName = characterName,
            onClick = onClick
        ) },
        isBattle = true,
    )
}

@Composable
fun HoldInBattleTutorial(
    battle: Battle,
    characterName: String,
    onClick: () -> Unit,
    winner: Boolean,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){
    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Holding will restore or empower your DEF.",
            subText = "> Hold to defend from the opponents attack.\n> You cannot Hold in two consecutive turns. Attack your enemy.",
            phase = TurnPhase.MOVEMENT,
            canEnd = winner,
            endTutorial = endTutorial
        ) },
        battle = { BattleTutorial(
            getDrawable = getDrawable,
            battleOptions = BattleOptions(attack = battle.currentTurn >= 2u, hold = battle.currentTurn == 1u),
            battle = battle,
            end = winner,
            characterName = characterName,
            onClick = onClick
        ) },
        isBattle = true,
    )
}

@Composable
fun FleeInBattleTutorial(
    battle: Battle,
    characterName: String,
    onClick: () -> Unit,
    winner: Boolean,
    getDrawable: suspend (String) -> ImageBitmap,
    endTutorial: () -> Unit
){
    TutorialScreenFrame(
        header = { HeaderTutorial(
            height = 75.dp,
            mainText = "Fleeing a battle will maintain your items.",
            subText = "> If you do not lose HP after Holding, your chance to flee a battle will rise .\n> Hold the first turn, and then Flee the battle.",
            phase = TurnPhase.MOVEMENT,
            canEnd = winner,
            endTutorial = endTutorial
        ) },
        battle = { BattleTutorial(
            getDrawable = getDrawable,
            battleOptions = BattleOptions(flee = battle.currentTurn >= 2u, hold = battle.currentTurn == 1u),
            battle = battle,
            end = winner,
            characterName = characterName,
            onClick = onClick
        ) },
        isBattle = true,
    )
}