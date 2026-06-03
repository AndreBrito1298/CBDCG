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
import isel.pt.cbdcg.domain.game.board.getAdjacent
import isel.pt.cbdcg.domain.game.board.getBlocked
import isel.pt.cbdcg.viewmodel.GameUIState

interface BoardTilePossibleActions{
    object InspectTileEffect : BoardTilePossibleActions
    object InspectCharacter : BoardTilePossibleActions
    object PlaceCard : BoardTilePossibleActions
    object MoveCharacter : BoardTilePossibleActions
    object ApplyMovement : BoardTilePossibleActions
}

data class BoardTileDDM(
    val inspectTileEffect: Boolean,
    val inspectCharacter: Boolean,
    val placeCharacter: Boolean,
    val equipItem: Boolean,
    val moveCharacter: Boolean,
    val applyMovement: Boolean
)

@Composable
fun Board(
    player: Player?,
    gameState: GameUIState,
    gameBoard: BoardTiles,
    tileSize: Dp,
    placeCard: (BoardPosition) -> Unit,
    inspect: (Card) -> Unit,
    moveSignal: (BoardTile) -> Unit,
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
                    val boardTile = gameBoard.find { it.pos == position }

                    if(boardTile != null){

                        val adjTiles = boardTile.tile.getAdjacent(gameBoard, boardTile.pos)
                        val blocked = boardTile.tile.getBlocked(adjTiles)
                        val tileName = boardTile.tile.toString() +
                            if(blocked.isNotEmpty()) "_" + blocked.map{ it.name[0] }.joinToString("")
                            else ""

                        val character = boardTile.character

                        val actions = BoardTileDDM(
                            inspectTileEffect = gameState is GameUIState.Idle &&
                                    boardTile.tile.specialEffect.type.name != "None" &&
                                    boardTile.tile.specialEffect.type.name != "Start",
                            inspectCharacter = gameState is GameUIState.Idle &&
                                    character != null,
                            placeCharacter = gameState is GameUIState.PlacingCard &&
                                    gameState.card is CharacterCard,
                            equipItem = gameState is GameUIState.PlacingCard &&
                                    gameState.card is ItemCard,
                            moveCharacter = gameState is GameUIState.Idle &&
                                    character != null &&
                                    character.name == player?.currentCharacter,
                            applyMovement = gameState is GameUIState.MovingCharacter
                        )

                        val tilePath =
                            if(actions.applyMovement && boardTile in (gameState as GameUIState.MovingCharacter).path)
                                pathSegmentFor(gameState.path, boardTile)
                            else null

                        BoardTile(
                            actions = actions,
                            boardTile = boardTile,
                            tileName = tileName,
                            tileSize = tileSize,
                            tilePath = tilePath,
                            onClick = { action ->
                                when(action){
                                    is BoardTilePossibleActions.InspectTileEffect -> inspect(TileCard(boardTile.tile))
                                    is BoardTilePossibleActions.InspectCharacter -> inspect(CharacterCard(character!!))
                                    is BoardTilePossibleActions.PlaceCard -> placeCard(position)
                                    is BoardTilePossibleActions.MoveCharacter -> moveSignal(boardTile)
                                    is BoardTilePossibleActions.ApplyMovement -> moveCharacter(boardTile)
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