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
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.BoardTiles
import isel.pt.cbdcg.domain.game.board.getAdjacent
import isel.pt.cbdcg.domain.game.board.getBlocked
import isel.pt.cbdcg.viewmodel.GameUIState

data class BoardTileDrawConditions(
    val placingTile: Boolean,
    val placingCharacter: Boolean,
    val equippingItem: Boolean,
    val characterIsSelected: Boolean,
    val characterCanMove: Boolean,
    val characterIsMoving: Boolean,
)

@Composable
fun Board(
    player: Player?,
    gameState: GameUIState,
    gameBoard: BoardTiles,
    tileSize: Dp,
    placeCard: (BoardPosition) -> Unit,
    selectBoardCharacter: (BoardTile) -> Unit,
    inspectCharacter: (Card) -> Unit,
    moveSignal: () -> Unit,
    moveCharacter: (BoardTile) -> Unit,
) {

    val positions = gameBoard.map { it.pos }

    val selectedCharacter = (gameState as? GameUIState.SelectBoardCharacter)?.position?.character
    val (isTileCard, isCharacterCard, isItemCard) =
        CardType.entries.map{ type -> (gameState as? GameUIState.PlacingCard)?.card?.type == type }

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
                        val tilePath =
                            if(gameState is GameUIState.MovingCharacter && boardTile in gameState.path)
                                pathSegmentFor(gameState.path, boardTile)
                            else null

                        val drawConditions = BoardTileDrawConditions(
                            placingTile = isTileCard,
                            placingCharacter = isCharacterCard,
                            equippingItem = isItemCard,
                            characterIsSelected = selectedCharacter == character && character != null,
                            characterCanMove = selectedCharacter == character && character != null && character.name == player?.currentCharacter,
                            characterIsMoving = gameState is GameUIState.MovingCharacter,
                        )

                        BoardTile(
                            conditions = drawConditions,
                            boardTile = boardTile,
                            tileName = tileName,
                            tileSize = tileSize,
                            tilePath = tilePath,
                            onClick = { if(!drawConditions.characterIsMoving) placeCard(position) else moveCharacter(boardTile) },
                            selectCharacter = { selectBoardCharacter(boardTile) },
                            inspectCharacter = { if(character != null) inspectCharacter(CharacterCard(character)) },
                            moveSignal = moveSignal,
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