package isel.pt.cbdcg.views.game

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.connectionDistancesFrom
import isel.pt.cbdcg.domain.game.board.tile.isPositive
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.viewmodel.GameUI
import isel.pt.cbdcg.viewmodel.GameUIState
import isel.pt.cbdcg.views.game.utils.dialog.GameOverDialog
import isel.pt.cbdcg.views.game.utils.board.Board
import isel.pt.cbdcg.views.game.utils.players.PlayerHand
import isel.pt.cbdcg.views.game.utils.board.ZoomButtons
import isel.pt.cbdcg.views.game.utils.dialog.CardStatsDialog
import isel.pt.cbdcg.views.game.utils.dialog.TileEffectDialog
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.views.game.utils.dialog.BattleDialog
import isel.pt.cbdcg.views.game.utils.dialog.CharacterCollisionDialog
import isel.pt.cbdcg.views.game.utils.dialog.ChooseTargetDialog
import isel.pt.cbdcg.views.game.utils.dialog.CollisionOption
import isel.pt.cbdcg.views.game.utils.dialog.EndBattleDialog
import isel.pt.cbdcg.views.game.utils.dialog.StartBattleDialog
import isel.pt.cbdcg.views.game.utils.misc.extra.SimpleClock
import isel.pt.cbdcg.views.game.utils.misc.layout.GameScreenHeader
import kotlinx.coroutines.delay
import kotlin.time.Clock
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GameScreen(
    player: Player,
    game: Game,
    gameUI: GameUI,
    selectCard: (UInt, Card) -> Unit,
    placeSignal: () -> Unit,
    placeOnBoard: (BoardPosition) -> Unit,
    unequip: (Int) -> Unit,
    toggleCardStats: (Card?, BoardTile?) -> Unit,
    onEffectInfoClick: () -> Unit,
    moveSignal: (BoardTile) -> Unit,
    moveCharacter: (BoardTile) -> Unit,
    battleSignal: (Character, Character) -> Unit,
    challenge: () -> Unit,
    sneak: () -> Unit,
    rotateTile: (Boolean) -> Unit,
    zoom: (Boolean) -> Unit,
    nextPhase: () -> Unit,
    closeDialog: (Boolean) -> Unit,
    endBattle: () -> Unit,
    attackTarget: (Character?) -> Unit,
    battleAction: (PossibleBattleActions?) -> Unit,
    participateInBattle: (Boolean) -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    leaveGame: () -> Unit,
) {

    val currentPlayer = game.players.find {
        it.user.id == game.turn.playerTurn.firstOrNull()
    }

    var remainingSeconds by remember(game.turn.deadline) {
        mutableStateOf(((game.turn.deadline - Clock.System.now().toEpochMilliseconds()) / 1000).coerceAtLeast(0))
    }

    LaunchedEffect(game.turn.deadline) {
        while (true) {
            remainingSeconds = ((game.turn.deadline - Clock.System.now().toEpochMilliseconds()) / 1000).coerceAtLeast(0)
            if (remainingSeconds <= 0) break
            delay(1000.milliseconds)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ){

        GameScreenHeader(
            game = game,
            currentPlayer = currentPlayer,
            thisPlayer = player,
            movementUsed = gameUI.movementUsed,
            nextPhase = nextPhase,
            leaveGame = leaveGame
        )

        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
            ) {

                Box(
                    modifier = Modifier
                        .weight(2f)
                        .border(2.dp, Color.Black)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    Board(
                        player = player,
                        gameState = gameUI.state,
                        battledCharacterPositions = gameUI.battledCharactersPosition,
                        gameBoard = game.board.tiles,
                        tileSize = 128.dp * gameUI.boardZoom,
                        placeCard = { pos -> placeOnBoard(pos) },
                        inspect = { card, boardTile -> toggleCardStats(card, boardTile) },
                        moveSignal = { boardTile -> moveSignal(boardTile) },
                        battleSignal = { current, target -> battleSignal(current, target) },
                        moveCharacter = { pos -> moveCharacter(pos) },
                        idle = { closeDialog(false) },
                        getDrawable = { getDrawable(it) },
                    )

                    if(game.battle == null){
                        SimpleClock(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .size(50.dp)
                                .padding(4.dp),
                            remainingSeconds = remainingSeconds
                        )
                    }

                    ZoomButtons(
                        modifier = Modifier.align(Alignment.TopEnd).padding(8.dp),
                        amplify = { zoom(true) },
                        reduce = { zoom(false) },
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    PlayerHand(
                        getDrawable = { getDrawable(it) },
                        hand = player.hand,
                        selectCard = { idx, card -> selectCard(idx, card) },
                        selected = (gameUI.state as? GameUIState.SelectCard)?.idx,
                        placeCard = placeSignal,
                        inspectCard = { card -> toggleCardStats(card, null) },
                        rotateLeft = { rotateTile(false) },
                        rotateRight = { rotateTile(true) },
                    )
                }
            }
        }
    }

    when(gameUI.state){
        is GameUIState.InspectCard ->
            CardStatsDialog(
                getDrawable = { getDrawable(it) },
                card = gameUI.state.card,
                unequip = { idx -> unequip(idx) },
                onDismiss = { toggleCardStats(null, null) }
            )
        is GameUIState.InspectTileEffect -> {

            val affectedCharacters =
                gameUI.state.boardTile?.let { origin ->
                    val specialEffect = gameUI.state.tile.specialEffect
                    val range = specialEffect.range.toInt()
                    val distances = game.board.connectionDistancesFrom(origin)

                    distances
                        .filter { if(specialEffect.isPositive()) it.value <= range else it.value in 1..range }.keys
                        .mapNotNull { it.character }
                        .filterIsInstance<PlayableCharacter>()
                } ?: emptyList()

            TileEffectDialog(
                getDrawable = { getDrawable(it) },
                effect = gameUI.state.tile.specialEffect,
                activate = gameUI.state.activateInTile,
                onConfirm = onEffectInfoClick,
                affectedCharacters = affectedCharacters
            )
        }
        is GameUIState.GameOver ->
            GameOverDialog(
                winner = gameUI.state.winner,
                onDismiss = leaveGame
            )
        is GameUIState.CharacterCollision ->
            CharacterCollisionDialog(
                    getDrawable = { getDrawable(it) },
                    remainingSeconds = remainingSeconds,
                    movingCharacter = gameUI.state.playerCharacter,
                    staticCharacter = gameUI.state.enemyCharacter,
                    canSneak = gameUI.state.playerCharacter.adjustStats().spe - gameUI.movementUsed >= 2,
                    onClick = { option ->
                        when (option) {
                            CollisionOption.COMBAT -> {
                                challenge()
                            }

                            CollisionOption.SNEAK -> {
                                sneak()
                            }

                            CollisionOption.CANCEL -> {
                                closeDialog(false)
                            }
                        }
                    },
                    onDismiss = { closeDialog(false) }
                )
        is GameUIState.StartBattle -> {
            StartBattleDialog(
                getDrawable = { getDrawable(it) },
                remainingSeconds = remainingSeconds,
                battle = gameUI.state.battle,
                myCharacter = gameUI.state.character,
                confirm = { accept -> participateInBattle(accept) }
            )
        }
        is GameUIState.InBattle -> {
            BattleDialog(
                getDrawable = { getDrawable(it) },
                remainingSeconds = remainingSeconds,
                battle = gameUI.state.battle,
                playerCharacterName = player.currentCharacter,
                attackTarget = { attackTarget(null) },
                onClick = { action ->
                    when(action) {
                        PossibleBattleActions.HOLD -> battleAction(PossibleBattleActions.HOLD)
                        PossibleBattleActions.FLEE -> battleAction(PossibleBattleActions.FLEE)
                        PossibleBattleActions.ATTACK -> return@BattleDialog
                        null -> battleAction(null)
                        PossibleBattleActions.PASSIVE -> battleAction(null)
                    }
                },
                onDismiss = {  }
            )
        }
        is GameUIState.Attacking -> {
            ChooseTargetDialog(
                getDrawable = { getDrawable(it) },
                characters = gameUI.state.battle.characters.filter{ it.name != player.currentCharacter && it.adjustStats().hp > 0 },
                targetCharacter = gameUI.state.target,
                target = { target -> attackTarget(target) },
                attack = { battleAction(PossibleBattleActions.ATTACK) },
                onDismiss = { closeDialog(true) }
            )
        }
        is GameUIState.EndBattle -> {
            EndBattleDialog(
                getDrawable = { getDrawable(it) },
                remainingSeconds = remainingSeconds,
                player = player,
                isWinner = player.currentCharacter == gameUI.state.winner.currentCharacter,
                isBattling = player.user.id in (gameUI.state.losers + gameUI.state.fled + gameUI.state.winner).map { it.user.id },
                fled = player.user.id in gameUI.state.fled.map { it.user.id },
                bet = gameUI.state.bet,
                ready = gameUI.state.readyToLeave,
                total = gameUI.state.losers.size + gameUI.state.fled.size + 1,
                confirm = { endBattle() }
            )
        }
        is GameUIState.CharacterEvolved -> {
            CardStatsDialog(
                getDrawable = { getDrawable(it) },
                card = gameUI.state.character.toCard(),
                unequip = { idx -> unequip(idx) },
                onDismiss = { toggleCardStats(null, null) }
            )
        }
        else -> {  }
    }
}
