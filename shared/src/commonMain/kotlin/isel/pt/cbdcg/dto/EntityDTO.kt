package isel.pt.cbdcg.dto

import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.Entity
import isel.pt.cbdcg.domain.game.board.toBoardTile
import isel.pt.cbdcg.domain.game.board.toBoardTileDTO
import isel.pt.cbdcg.domain.game.toPlayer
import isel.pt.cbdcg.domain.game.toPlayerDTO
import kotlinx.serialization.Serializable

@Serializable
data class EntityDTO(
    val type: String,
    val player: PlayerDTO? = null,
    val boardTile: BoardTileDTO? = null,
){
    fun toType(): Entity =
        when(type){
            "Player" -> player!!.toPlayer()
            "BoardTile" -> boardTile!!.toBoardTile()
            else -> throw Exception("Invalid Entity Type")
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

inline fun <reified T: Entity>toEntityDTO(entity: T): EntityDTO{
    return when(T::class){
        BoardTile::class -> EntityDTO("BoardTile", null, (entity as BoardTile).toBoardTileDTO())
        Player::class -> EntityDTO("Player", (entity as Player).toPlayerDTO(), null)
        else -> throw Exception("Invalid Entity Type")
    }
}
