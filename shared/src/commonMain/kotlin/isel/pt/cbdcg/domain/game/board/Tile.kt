package isel.pt.cbdcg.domain.game.board

import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.dto.TileDTO
import isel.pt.cbdcg.dto.TileEffectDTO
import isel.pt.cbdcg.error.GameError

enum class StatType { Hp, Dmg, Def, Spe }

sealed interface TileEffectTypes {
    data object None : TileEffectTypes
    data object Start : TileEffectTypes
    data object Chest : TileEffectTypes
    data object BigChest : TileEffectTypes
    data class StatUp(val stat: StatType) : TileEffectTypes
    data class StatDown(val stat: StatType) : TileEffectTypes
    data class StatUpAoE(val stat: StatType) : TileEffectTypes
    data class StatDownAoE(val stat: StatType) : TileEffectTypes

    val name: String get() = when (this) {
        is StatUp -> "${stat.name}Up"
        is StatDown -> "${stat.name}Down"
        is StatUpAoE -> "${stat.name}UpAoE"
        is StatDownAoE -> "${stat.name}DownAoE"
        else -> this::class.simpleName ?: ""
    }
}

data class TileEffect(
    val type: TileEffectTypes = TileEffectTypes.None,
    val maxCooldown: UInt = 0u,
    val info: String = ""
)

fun String.effectType(): TileEffectTypes {

    if (this == "None") return TileEffectTypes.None
    if (this == "Start") return TileEffectTypes.Start
    if (this == "Chest") return TileEffectTypes.Chest
    if (this == "BigChest") return TileEffectTypes.BigChest

    val stat = StatType.entries.find { this.startsWith(it.name) }
    if (stat != null) {
        val suffix = this.substring(stat.name.length)
        return when (suffix) {
            "Up" -> TileEffectTypes.StatUp(stat)
            "Down" -> TileEffectTypes.StatDown(stat)
            "UpAoE" -> TileEffectTypes.StatUpAoE(stat)
            "DownAoE" -> TileEffectTypes.StatDownAoE(stat)
            else -> throw GameError.InvalidFormat("Tile Effect", this)
        }
    }
    throw GameError.InvalidFormat("Tile Effect", this)
}
fun TileEffect.getStatModifier(): StatModifier {

    val statType = when(this.type){
        is TileEffectTypes.StatDown -> this.type.stat
        is TileEffectTypes.StatDownAoE -> this.type.stat
        is TileEffectTypes.StatUp -> this.type.stat
        is TileEffectTypes.StatUpAoE -> this.type.stat
        else -> throw GameError.InvalidFormat("Tile Effect", this.type.name)
    }

    val statChange = when(this.type){
        is TileEffectTypes.StatDown -> -1
        is TileEffectTypes.StatDownAoE -> -2
        is TileEffectTypes.StatUp -> +1
        is TileEffectTypes.StatUpAoE -> +2
    }

    return StatModifier(
        stats = Stats(
            hp = if(statType == StatType.Hp) statChange else 0,
            dmg = if(statType == StatType.Dmg) statChange else 0,
            def = if(statType == StatType.Def) statChange else 0,
            spe = if(statType == StatType.Spe) statChange else 0
        ),
        duration = 2u
    )
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
) {
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

    val statUp = StatType.entries.associate{ stat ->
        TileEffect(
            type = TileEffectTypes.StatUp(stat),
            maxCooldown = 2u,
            info = "Until the end of your next turn, this character has +★ in ${stat.name}."
        ) to 1u
    }

    val statDown = StatType.entries.associate { stat ->
        TileEffect(
            type = TileEffectTypes.StatDown(stat),
            maxCooldown = 2u,
            info = "Until the end of your next turn, this character has -★ in ${stat.name}."
        ) to 1u
    }

    val statUpAoE = StatType.entries.associate { stat ->
        TileEffect(
            type = TileEffectTypes.StatUpAoE(stat),
            maxCooldown = 4u,
            info = "Until the end of your next turn, all player characters within a 4 tile radius have +★★ in ${stat.name}."
        ) to 1u
    }

    val statDownAoE = StatType.entries.associate { stat ->
        TileEffect(
            type = TileEffectTypes.StatDownAoE(stat),
            maxCooldown = 4u,
            info = "Until the end of your next turn, all player characters within a 4 tile radius have -★★ in ${stat.name}."
        ) to 1u
    }

    val allTileEffects = mapOf(
        TileEffect(
            type = TileEffectTypes.Chest,
            maxCooldown = 2u,
            info = "Draw one ITEM card from the Item Deck.\nCooldown: 2 Dungeon Turns\nRestriction: ---"
        ) to 6u,
        TileEffect(
            type = TileEffectTypes.BigChest,
            maxCooldown = 3u,
            info = "Draw two ITEMS cards from the Item Deck.\nCooldown: 3 Dungeon Turns\nRestriction: ---"
        ) to 3u
    ) + statUp + statDown + statUpAoE + statDownAoE
}