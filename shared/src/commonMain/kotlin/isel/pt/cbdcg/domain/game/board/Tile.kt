package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.dto.TileDTO
import isel.pt.cbdcg.dto.TileEffectDTO
import isel.pt.cbdcg.error.GameError

enum class TileEffectTypes{
    None, Start, Chest, BigChest
}

data class TileEffect(
    val type: TileEffectTypes = TileEffectTypes.None,
    val maxCooldown: UInt = 0u,
    val info: String = ""
)

fun String.effectType(): TileEffectTypes =
    when(this){
        "None" -> TileEffectTypes.None
        "Start" -> TileEffectTypes.Start
        "Chest" -> TileEffectTypes.Chest
        "BigChest" -> TileEffectTypes.BigChest
        else -> throw GameError.InvalidFormat("Tile Effect", this)
    }

fun TileEffect.toTileEffectDTO(): TileEffectDTO =
    TileEffectDTO(
        type = type.name,
        maxCooldown = maxCooldown.toInt(),
        info = info
    )

fun TileEffectDTO.toTileEffect(): TileEffect =
    TileEffect(
        type = type.effectType(),
        maxCooldown = maxCooldown.toUInt(),
        info = info
    )

data class Tile(
    val connections: List<Direction>,
    val specialEffect: TileEffect = TileEffect()
) : Entity {
    override fun toString(): String =
        connections.map { it.name[0] }.sorted().joinToString("")
}

fun Tile.canConnectTo(dir: Direction, tile: Tile): Boolean{

    if(!this.connections.contains(dir)) return false

    return tile.connections.contains(dir.opposite())
}
fun Tile.rotate(right: Boolean): Tile =
    this.copy(
        connections = connections.map { direction ->
            if (right) direction.rotateRight()
            else direction.rotateLeft()
        }
    )
fun Tile.getBlocked(adjTiles: List<Pair<Direction, BoardTile>>): List<Direction> =
    adjTiles.mapNotNull{
        if(!canConnectTo(it.first, it.second.tile)) it.first
        else null
    }
fun Tile.getAdjacent(tiles: BoardTiles, targetPos: BoardPosition): List<Pair<Direction, BoardTile>> =
    connections.mapNotNull { dir ->
        val neighbourPos = targetPos.neighbour(dir)
        val neighbourTile = tiles.find { it.pos == neighbourPos }

        if(neighbourTile != null) dir to neighbourTile
        else null
    }

fun Tile.toTileDTO(): TileDTO =
    TileDTO(
        connections = connections.map { it.name[0].toString() }.toTypedArray(),
        specialEffect = specialEffect.toTileEffectDTO()
    )

fun TileDTO.toTile(): Tile =
    Tile(
        connections = connections.map{ it.toDirection() },
        specialEffect = specialEffect.toTileEffect()
    )


object AllTileEffects{
    val allTileEffects = mapOf(
        TileEffect(
            type = TileEffectTypes.Chest,
            maxCooldown = 2u,
            info = "Draw one ITEM card from the Item Deck.\nCooldown: 2 Dungeon Turns\nRestriction: ---"
        ) to 6u,
        TileEffect(
            type = TileEffectTypes.BigChest,
            maxCooldown = 2u,
            info = "Draw two ITEMS cards from the Item Deck.\nCooldown: 3 Dungeon Turns\nRestriction: ---"
        ) to 3u
    )
}