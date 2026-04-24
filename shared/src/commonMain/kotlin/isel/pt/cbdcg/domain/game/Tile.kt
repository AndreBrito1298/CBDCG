package isel.pt.cbdcg.domain.game

data class Tile(
    val connections: List<Direction>,
    val blocked: List<Direction> = emptyList(),
){

    fun canConnectTo(dir: Direction, tile: Tile): Boolean{

        if(!this.connections.contains(dir)) return false

        return tile.connections.contains(dir.opposite())
    }

    fun rotate(right: Boolean): Tile =
        Tile(connections.map { direction ->
            if (right) direction.rotateRight()
            else direction.rotateLeft()
        })

}

fun Tile.codeString(): String {

    val connections = connections.map { it.name[0] }.sorted().joinToString("")
    val blocked = blocked.map { it.name[0] }.sorted().joinToString("")
    return if(blocked.isNotBlank()) "${connections}_${blocked}" else connections
}

fun String.decodeTile(): Tile {

    val (connectionsString, blockedString) = split("_")
    val connections = connectionsString.map{ it.toDirection() }
    val blocked = blockedString.map{ it.toDirection() }

    return Tile(connections, blocked)
}