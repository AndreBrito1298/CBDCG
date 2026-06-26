package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CardType
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.BoardTiles
import isel.pt.cbdcg.domain.game.board.tile.getAdjacent
import isel.pt.cbdcg.domain.game.board.tile.getBlocked
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.viewmodel.GameUIState

enum class BoardTilePossibleActions{
    InspectTileEffect,
    InspectCharacter,
    PlaceCard,
    MoveCharacter,
    Challenge,
    ApplyMovement
}

data class BoardTileDDM(
    val inspectTileEffect: Boolean,
    val inspectCharacter: Boolean,
    val placeCharacter: Boolean,
    val equipItem: Boolean,
    val moveCharacter: Boolean,
    val battleCharacter: Boolean,
    val sneakThrough: Boolean,
    val applyMovement: Boolean,
    val wasBattled: Boolean,
    val myCharacter: Boolean
)

@Composable
fun Board(
    player: Player?,
    gameState: GameUIState,
    battledCharacterPositions: List<BoardPosition>,
    gameBoard: BoardTiles,
    tileSize: Dp,
    placeCard: (BoardPosition) -> Unit,
    inspect: (Card, BoardTile? ) -> Unit,
    moveSignal: (BoardTile) -> Unit,
    battleSignal: (Character, Character) -> Unit,
    moveCharacter: (BoardTile) -> Unit,
) {

    val positions = gameBoard.map { it.pos }

    val minX = positions.minOf { it.x } - 1
    val maxX = positions.maxOf { it.x } + 1
    val minY = positions.minOf { it.y } - 1
    val maxY = positions.maxOf { it.y } + 1

    Column(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .verticalScroll(rememberScrollState())
    ) {

        for (y in maxY downTo minY) {
            Row{
                for (x in minX..maxX) {

                    val position = BoardPosition(x,y)
                    val currentBoardTile = gameBoard.find { it.pos == position }

                    if(currentBoardTile != null){

                        val adjTiles = currentBoardTile.tile.getAdjacent(gameBoard, currentBoardTile.pos)
                        val blocked = currentBoardTile.tile.getBlocked(adjTiles)
                        val tileName = currentBoardTile.tile.toString() +
                            if(blocked.isNotEmpty()) "_" + blocked.map{ it.name[0] }.joinToString("")
                            else ""

                        val character = currentBoardTile.character
                        val wasBattled = battledCharacterPositions.none{ it.equals(currentBoardTile.pos) }

                        val adjacentCharacters = currentBoardTile.tile
                            .getAdjacent(gameBoard, currentBoardTile.pos)
                            .mapNotNull{ it.second.character }
                        val playerCharacter = adjacentCharacters.find{ it.name == player?.currentCharacter }

                        val actions = BoardTileDDM(
                            inspectTileEffect = gameState is GameUIState.Idle &&
                                    currentBoardTile.tile.specialEffect.type.name != "None" &&
                                    currentBoardTile.tile.specialEffect.type.name != "Start",

                            inspectCharacter = gameState is GameUIState.Idle &&
                                    character != null,

                            placeCharacter = gameState is GameUIState.PlacingCard &&
                                    gameState.card is CharacterCard &&
                                    (character == null || character.name == player?.currentCharacter),

                            equipItem = gameState is GameUIState.PlacingCard &&
                                    gameState.card is ItemCard,

                            moveCharacter = gameState is GameUIState.Idle &&
                                    character != null &&
                                    character.name == player?.currentCharacter,

                            battleCharacter = gameState is GameUIState.Idle &&
                                    character != null &&
                                    playerCharacter != null &&
                                    character.name != playerCharacter.name &&
                                    wasBattled,


                            sneakThrough = gameState is GameUIState.SneakDestination &&
                                    gameState.targets.find { it.pos == currentBoardTile.pos } != null,

                            applyMovement = gameState is GameUIState.MovingCharacter,

                            wasBattled = wasBattled,

                            myCharacter = character != null && character.name == player?.currentCharacter
                        )

                        val tilePath =
                            if(actions.applyMovement && currentBoardTile in (gameState as GameUIState.MovingCharacter).path)
                                pathSegmentFor(gameState.path, currentBoardTile)
                            else null

                        BoardTile(
                            actions = actions,
                            boardTile = currentBoardTile,
                            tileName = tileName,
                            tileSize = tileSize,
                            tilePath = tilePath,
                            onClick = { action ->
                                when(action){
                                    BoardTilePossibleActions.InspectTileEffect -> inspect(TileCard(currentBoardTile.tile), currentBoardTile)
                                    BoardTilePossibleActions.InspectCharacter -> inspect(character.toCard(), currentBoardTile)
                                    BoardTilePossibleActions.PlaceCard -> placeCard(position)
                                    BoardTilePossibleActions.MoveCharacter -> moveSignal(currentBoardTile)
                                    BoardTilePossibleActions.Challenge -> battleSignal(requireNotNull(playerCharacter), requireNotNull(character))
                                    BoardTilePossibleActions.ApplyMovement -> moveCharacter(currentBoardTile)
                                }
                            }
                        )
                    }
                    else EmptyBoardTile(
                        seeGrid = gameState is GameUIState.PlacingCard && gameState.card.type == CardType.TILE,
                        tileSize = tileSize
                    ){ placeCard(position) }
                }
            }
        }
    }
}