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

    val string = split("_")
    val connections = string[0].map{ it.toDirection() }
    val blocked = if(string.size == 2) string[1].map{ it.toDirection() } else emptyList()

    return Tile(connections, blocked)
}