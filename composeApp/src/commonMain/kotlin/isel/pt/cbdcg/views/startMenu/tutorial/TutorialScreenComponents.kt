package isel.pt.cbdcg.views.startMenu.tutorial

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.PlayerHand
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.TurnPhase
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.BoardTiles
import isel.pt.cbdcg.domain.game.board.getTileName
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.board.BoardTilePossibleActions
import isel.pt.cbdcg.views.game.utils.board.MovementPathOverlay
import isel.pt.cbdcg.views.game.utils.board.TilePathSegment
import isel.pt.cbdcg.views.game.utils.board.pathSegmentFor
import isel.pt.cbdcg.views.game.utils.dialog.CharacterCollisionInfo
import isel.pt.cbdcg.views.game.utils.dialog.CollisionOption
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage
import isel.pt.cbdcg.views.game.utils.misc.info.ActionsInfo
import isel.pt.cbdcg.views.game.utils.misc.info.BattleInfo
import kotlin.collections.component1
import kotlin.collections.component2

@Composable
fun TutorialScreenFrame(
    header: @Composable () -> Unit,
    board: @Composable BoxScope.() -> Unit = {},
    battle: @Composable BoxScope.() -> Unit = {},
    isBattle: Boolean = false,
    buttons: @Composable BoxScope.() -> Unit = {},
    playerHand: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        header()

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {

                if(!isBattle){
                    Box(
                        modifier = Modifier
                            .weight(2f)
                            .border(2.dp, Color.Black)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        board()
                        buttons()
                    }

                    playerHand()

                } else {
                    Box(
                        modifier = Modifier.border(2.dp, Color.Black).padding(4.dp),
                        contentAlignment = Alignment.Center
                    ){ battle() }
                }
            }
        }
    }
}
@Composable
fun HeaderTutorial(
    height: Dp,
    mainText: String,
    subText: String,
    phase: TurnPhase = TurnPhase.CONSTRUCTION,
    nextPhaseEnabled: Boolean = false,
    canEnd: Boolean,
    nextPhase: () -> Unit = {},
    endTutorial: () -> Unit
){

    val nextPhaseText = when(phase){
        TurnPhase.CONSTRUCTION -> "Next: Substitution"
        TurnPhase.SUBSTITUTION -> "Next: Movement"
        TurnPhase.MOVEMENT -> "End Turn"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(height),
        contentAlignment = Alignment.CenterStart,
    ){
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if(!canEnd){
                Text(
                    text = mainText,
                    style = MaterialTheme.typography.titleSmall
                )

                Text(
                    text = subText,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Button(enabled = canEnd, onClick = endTutorial) {
                    Text("End Tutorial")
                }
            }
        }

        if(!canEnd){
            Button(
                enabled = nextPhaseEnabled,
                onClick = nextPhase,
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Text(nextPhaseText)
            }
        }
    }
}
@Composable
fun ZoomButtonsTutorial(
    modifier: Modifier = Modifier,
    amplify: () -> Unit,
    reduce: () -> Unit,
    maxZoom: Boolean,
    minZoom: Boolean,
){

    val plusColor = if(maxZoom) Color.Green else Color.Black
    val minusColor = if(minZoom) Color.Green else Color.Black

    Row(
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = reduce,
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(0.dp),
            colors = outlinedButtonColors(
                containerColor = Color.White,
                contentColor = minusColor
            ),
            border = BorderStroke(1.dp, minusColor)
        ) {
            Text("-", color = minusColor)
        }

        Spacer(modifier = Modifier.width(4.dp))

        OutlinedButton(
            onClick = amplify,
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(0.dp),
            colors = outlinedButtonColors(
                containerColor = Color.White,
                contentColor = plusColor
            ),
            border = BorderStroke(1.dp, plusColor)
        ) {
            Text("+", color = plusColor)
        }
    }
}
@Composable
fun PlayerHandTutorial(
    modifier: Modifier,
    hand: PlayerHand,
    getDrawable: suspend (String) -> ImageBitmap,
    isLocked: (Card) -> Boolean,
    dropDownMenu: (Card) -> CardDDM,
    inspect: (Card) -> Unit = {},
    rotate: (Boolean) -> Unit = {},
    place: (Card) -> Unit = {}
) {

    var open by remember{ mutableStateOf<UInt?>(null) }

    Box(modifier = modifier){
        Row(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            hand.forEach { (index, card) ->

                val fileName = when (card) {
                    is CharacterCard -> card.character.name
                    is ItemCard -> card.item.name
                    is TileCard -> card.tile.toString()
                }
                val effectName = if (card is TileCard) card.tile.specialEffect.type.name else null

                val cardDDM = dropDownMenu(card)
                val canClick = !isLocked(card) && cardDDM.canOpen()

                Box(
                    modifier = Modifier
                        .border(1.dp, if (canClick) Color.Black else Color.Green)
                        .padding(8.dp)
                        .then(
                            other =
                                if (canClick) Modifier.clickable { open = if (open == index) null else index }
                                else Modifier
                        )
                ) {
                    ZoomedImage(
                        fileName = fileName,
                        loadDrawable = { getDrawable(fileName) },
                        modifier = Modifier.size(128.dp),
                        zoom = 1.0f
                    )

                    if (effectName != null && effectName != "None" && effectName != "Start") {
                        ZoomedImage(
                            fileName = effectName,
                            loadDrawable = { getDrawable(effectName) },
                            modifier = Modifier.size(128.dp),
                            zoom = 0.33f
                        )
                    }

                    DropdownMenu(
                        expanded = open == index && canClick,
                        onDismissRequest = { open = null }
                    ) {
                        if (cardDDM.inspectCard) {
                            val inspectText = when (card) {
                                is CharacterCard -> "Inspect Character"
                                is ItemCard -> "Inspect Item"
                                is TileCard -> "Inspect Tile Effect"
                            }
                            DropdownMenuItem(
                                text = { Text(inspectText) },
                                onClick = { inspect(card) }
                            )
                        }
                        if (cardDDM.placeCard) {
                            val placeText = when (card) {
                                is CharacterCard -> "Place Character"
                                is ItemCard -> "Equip"
                                is TileCard -> "Place Tile"
                            }
                            DropdownMenuItem(
                                text = { Text(placeText) },
                                onClick = { place(card) }
                            )
                        }
                        if (cardDDM.rotateTile) {
                            DropdownMenuItem(
                                text = { Text("Rotate Left") },
                                onClick = { rotate(true) }
                            )

                            DropdownMenuItem(
                                text = { Text("Rotate Right") },
                                onClick = { rotate(false) }
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun BoardTutorial(
    zoom: Float,
    board: BoardTiles,
    path: List<BoardTile> = emptyList(),
    highlightTile: (BoardTile) -> Boolean = { false },
    executableActions: (BoardPosition) -> TutorialBoardTileState,
    getDrawable: suspend (String) -> ImageBitmap,
    onClick: (BoardPosition) -> Unit
){

    val positions = board.map { it.pos }

    val minX = positions.minOf { it.x } - 1
    val maxX = positions.maxOf { it.x } + 1
    val minY = positions.minOf { it.y } - 1
    val maxY = positions.maxOf { it.y } + 1

    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
    ){
        for (y in maxY downTo minY) {
            Row {
                for (x in minX..maxX) {
                    val position = BoardPosition(x,y)
                    val actions = executableActions(position)
                    val currentBoardTile = board.find { it.pos == position }

                    if(currentBoardTile != null){
                        val tileName = currentBoardTile.getTileName(board)
                        val tilePath =
                            if(path.isNotEmpty()) pathSegmentFor(path, currentBoardTile)
                            else null

                        Box(
                            Modifier.then( other =
                                if(highlightTile(currentBoardTile)) Modifier.border(1.dp, Color.Green)
                                else Modifier )
                        ){
                            TutorialBoardTile(
                                actions = actions,
                                boardTile = currentBoardTile,
                                tileName = tileName,
                                tileSize = 128.dp * zoom,
                                tilePath = tilePath,
                                getDrawable = getDrawable,
                                onClick = { onClick(currentBoardTile.pos) }
                            )
                        }

                    }
                    else { TutorialEmptyBoardTile(enabled = actions.placeTile, tileSize = 128.dp * zoom){ onClick(position) } }
                }
            }
        }
    }
}

@Composable
fun TutorialBoardTile(
    actions: TutorialBoardTileState,
    boardTile: BoardTile,
    tileName: String,
    tileSize: Dp,
    tilePath: TilePathSegment?,
    getDrawable: suspend (String) -> ImageBitmap,
    onClick: (BoardTilePossibleActions) -> Unit,
){
    var expanded by remember { mutableStateOf(false) }
    val effectName = boardTile.tile.specialEffect.type.name
    val characterName = boardTile.character?.name

    val isClickable = actions.placeCharacter || actions.placeItem || actions.applyMovement ||
            actions.challenge || actions.moveCharacter || actions.inspectTileEffect  ||
            actions.inspectCharacter || actions.sneakThrough

    Box(
        modifier = Modifier
            .size(tileSize)
            .clickable(enabled = isClickable) {
                when {
                    actions.placeCharacter || actions.placeItem -> onClick(BoardTilePossibleActions.PlaceCard)
                    actions.highlightCharacter  && actions.applyMovement -> onClick(BoardTilePossibleActions.Idle)
                    actions.applyMovement || actions.sneakThrough -> onClick(BoardTilePossibleActions.ApplyMovement)
                    else -> expanded = true
                }
            },
        contentAlignment = Alignment.Center
    ){

        // Draw the Tile
        ZoomedImage(
            fileName = tileName,
            zoom = 1.0f,
            loadDrawable = { getDrawable(tileName) },
            modifier = Modifier.size(tileSize)
        )

        // Draw the Effect
        if(effectName != "None"){
            ZoomedImage(
                fileName = effectName,
                loadDrawable = { getDrawable(effectName) },
                zoom = 0.33f,
                modifier = Modifier.size(tileSize),
                filter =
                    boardTile.cooldown?.let {
                        if(it > 0u)
                            ColorFilter.colorMatrix(ColorMatrix().apply{ setToSaturation(0f) })
                        else null
                    }
            )
        }

        // Draw the yellow path
        if(tilePath != null) {
            MovementPathOverlay(
                segment = tilePath,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Draw the Character
        if(characterName != null){
            Box(
                modifier =
                    if(actions.highlightCharacter) Modifier.border(1.dp, Color.Cyan, shape = CircleShape)
                    else Modifier
            ){
                ZoomedImage(
                    fileName = characterName,
                    loadDrawable = { getDrawable(characterName) },
                    zoom = 0.33f,
                    modifier = Modifier.size(tileSize),
                    filter =
                        if(actions.grayFilter)
                            ColorFilter.colorMatrix(ColorMatrix().apply{ setToSaturation(0f) })
                        else null
                )
            }
        }

        // Draw the yellow dots
        if (actions.placeCharacter || actions.sneakThrough) {
            Box(
                modifier = Modifier
                    .size(tileSize / 3)
                    .background(
                        color = Color.Yellow.copy(alpha = 0.45f),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        color = Color.Yellow.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            if (actions.moveCharacter) {
                DropdownMenuItem(
                    text = { Text("Move Character") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.MoveCharacter)
                    }
                )
            }

            if (actions.challenge) {
                DropdownMenuItem(
                    text = { Text("Challenge") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.Challenge)
                    }
                )
            }
            if (actions.inspectTileEffect) {
                DropdownMenuItem(
                    text = { Text("Inspect Tile Effect") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.InspectTileEffect)
                    }
                )
            }
            if (actions.inspectCharacter) {
                DropdownMenuItem(
                    text = { Text("Inspect Character") },
                    onClick = {
                        expanded = false
                        onClick(BoardTilePossibleActions.InspectCharacter)
                    }
                )
            }
        }
    }
}
@Composable
fun TutorialEmptyBoardTile(
    enabled: Boolean,
    tileSize: Dp,
    placeTile: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(tileSize)
            .then(
                if (enabled) {
                    Modifier
                        .border(1.dp, Color.Gray)
                        .clickable(onClick = placeTile)
                } else {
                    Modifier
                }
            )
    )
}
@Composable
fun CharacterCollisionTutorialDialog(
    getDrawable: suspend (String) -> ImageBitmap,
    movingCharacter: Character,
    staticCharacter: Character,
    canSneak: Boolean,
    onClick: (CollisionOption) -> Unit,
    onDismiss: () -> Unit
){
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically){
                Box{
                    Text(
                        text = "Character Collision",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        text = {
            Box(modifier = Modifier.width(800.dp).height(340.dp)){
                CharacterCollisionInfo(
                    getDrawable,
                    Modifier.fillMaxSize(),
                    movingCharacter,
                    staticCharacter,
                    canSneak,
                    onClick
                )
            }
        },
        confirmButton = {  }
    )
}
@Composable
fun BattleTutorial(
    getDrawable: suspend (String) -> ImageBitmap,
    battleOptions: BattleOptions,
    battle: Battle,
    end: Boolean,
    characterName: String,
    onClick: () -> Unit,
){

    var showActions by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
    ){
        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ){
            Button(onClick = { showActions = !showActions }) { Text("Show Battle Actions") }
        }

        if(!showActions){
            BattleInfo(
                getDrawable = { getDrawable(it) },
                modifier = Modifier.fillMaxWidth().height(358.dp),
                playerCharacterName = characterName,
                characters = battle.characters,
                targetCharacter = null
            )
        } else {
            ActionsInfo(
                getDrawable = { getDrawable(it) },
                modifier = Modifier.fillMaxWidth().height(358.dp).padding(4.dp),
                characters = battle.characters,
                currentTurn = battle.currentTurn,
                actions = battle.actions
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth().height(40.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ){
            Button(onClick = onClick, enabled = battleOptions.attack && !end) { Text("Attack") }
            Button(onClick = onClick, enabled = battleOptions.hold && !end) { Text("Hold") }
            Button(onClick = onClick, enabled = battleOptions.flee && !end) { Text("Flee") }
        }
    }
}

data class CardDDM(
    val inspectCard: Boolean = false,
    val rotateTile: Boolean = false,
    val placeCard: Boolean = false
)
private fun CardDDM.canOpen(): Boolean =
    this.inspectCard || this.rotateTile || this.placeCard

data class TutorialBoardTileState(
    val inspectTileEffect: Boolean = false,
    val inspectCharacter: Boolean = false,
    val placeTile: Boolean = false,
    val placeCharacter: Boolean = false,
    val placeItem: Boolean = false,
    val applyMovement: Boolean = false,
    val highlightCharacter: Boolean = false,
    val grayFilter: Boolean = false,
    val moveCharacter: Boolean = false,
    val challenge: Boolean = false,
    val sneakThrough: Boolean = false,
)

data class BattleOptions(
    val attack: Boolean = false,
    val hold: Boolean = false,
    val flee: Boolean = false,
)