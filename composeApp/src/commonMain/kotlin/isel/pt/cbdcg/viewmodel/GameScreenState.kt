package isel.pt.cbdcg.viewmodel

import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleBet
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.character.Character

data class GameUI(
    val state: GameUIState,
    val boardZoom: Float = 1.0f,
    val movementUsed: Int = 0,
    val charactersBattled: List<String> = emptyList(),
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
        val playerCharacter: Character,
        val enemyCharacter: Character
    ) : GameUIState

    data class SneakDestination(
        val origin: BoardTile,
        val targets: List<BoardTile>
    ) : GameUIState

    data class StartBattle(
        val battle: Battle,
        val character: Character
    ) : GameUIState

    data class InBattle(
        val player: Player,
        val battle: Battle,
    ) : GameUIState

    data class Attacking(
        val player: Player,
        val battle: Battle,
        val target: Character
    ) : GameUIState

    data class EndBattle(
        val playerCharacter: Character,
        val winner: Player,
        val losers: List<Player>,
        val bet: List<BattleBet>,
        val readyToLeave: List<Player>
    ) : GameUIState

    data class GameOver(
        val winner: Player
    ) : GameUIState
}