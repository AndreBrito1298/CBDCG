package isel.pt.cbdcg.viewmodel

import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.character.Character

data class GameUI(
    val state: GameUIState,
    val boardZoom: Float = 1.0f,
    val movementUsed: Int = 0
)
sealed interface GameUIState {

    data object Idle : GameUIState

    data class SelectCard(
        val idx: UInt,
        val card: Card
    ) : GameUIState

    data class PlacingCard(
        val idx: UInt,
        val card: Card
    ) : GameUIState

    data class InspectCard(
        val card: Card,
        val previous: GameUIState = Idle
    ) : GameUIState

    data class InspectPlayer(
        val player: Player,
    ) : GameUIState

    data class MovingCharacter(
        val from: BoardTile,
        val path: List<BoardTile> = emptyList()
    ) : GameUIState

    data class InspectTileEffect(
        val tile: Tile,
        val boardTile: BoardTile? = null,
        val activateInTile: Boolean = false
    ) : GameUIState

    data class CharacterCollision(
        val movingCharacter: Character,
        val staticCharacter: Character
    ) : GameUIState

    data class InBattle(
        val battle: Battle
    ) : GameUIState

    data class GameOver(
        val winner: Player
    ) : GameUIState
}