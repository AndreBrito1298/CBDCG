package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.game.Battle
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.Entity
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.toBoardTile
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.domain.game.toBattle
import isel.pt.cbdcg.domain.game.toBattleAction
import isel.pt.cbdcg.domain.game.toCard
import isel.pt.cbdcg.domain.game.toPlayer
import kotlinx.serialization.Serializable
@Serializable
data class EntityDTO(
    val player: PlayerDTO? = null,
    val boardTile: BoardTileDTO? = null,
    val board: Array<BoardTileDTO>? = null,
    val character: CharacterDTO? = null,
    val card: CardDTO? = null,
    val battleAction: BattleActionDTO? = null,
    val battle: BattleDTO? = null,
){
    fun toType(): Entity{
        return if (player != null) player.toPlayer().toEntity<Player>()
        else if (boardTile != null) boardTile.toBoardTile().toEntity<BoardTile>()
        else if (board != null) Board(board.map{ it.toBoardTile() }).toEntity<Board>()
        else if (character != null) character.toCharacter().toEntity<Character>()
        else if (card != null) card.toCard().toEntity<Card>()
        else if (battleAction != null) battleAction.toBattleAction().toEntity<BattleAction>()
        else if (battle != null) battle.toBattle().toEntity<Battle>()
        else throw Exception("Invalid Entity Type")

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as EntityDTO

        if (player != other.player) return false
        if (boardTile != other.boardTile) return false
        if (!board.contentEquals(other.board)) return false
        if (character != other.character) return false
        if (card != other.card) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player?.hashCode() ?: 0
        result = 31 * result + (boardTile?.hashCode() ?: 0)
        result = 31 * result + (board?.contentHashCode() ?: 0)
        result = 31 * result + (character?.hashCode() ?: 0)
        result = 31 * result + (card?.hashCode() ?: 0)
        return result
    }


}


@Serializable
data class GameUpdaterDTO(
    val userId: Int,
    val gameId: Int,
    val token: String,
    val updaterName: String,
    val origin: EntityDTO,
    val target: Array<EntityDTO>,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as GameUpdaterDTO

        if (userId != other.userId) return false
        if (gameId != other.gameId) return false
        if (token != other.token) return false
        if (updaterName != other.updaterName) return false
        if (origin != other.origin) return false
        if (!target.contentEquals(other.target)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = userId
        result = 31 * result + gameId
        result = 31 * result + token.hashCode()
        result = 31 * result + updaterName.hashCode()
        result = 31 * result + origin.hashCode()
        result = 31 * result + target.contentHashCode()
        return result
    }
}

/*
inline fun <reified T: Entity>toEntityDTO(entity: T): EntityDTO{
    return when(T::class){
        BoardTile::class -> EntityDTO("BoardTile", null, (entity as BoardTile).toBoardTileDTO())
        Player::class -> EntityDTO("Player", (entity as Player).toPlayerDTO(), null)
        else -> throw Exception("Invalid Entity Type")
    }
}

 */


fun Entity.toEntityDTO(): EntityDTO = this.toEntityDTO()