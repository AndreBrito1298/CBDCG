package isel.pt.cbdcg.domain.game.board

data class BoardPosition(val x: Int, val y: Int){
    override fun toString(): String = "$x,$y"
    override fun equals(other: Any?): Boolean {
        if(other !is BoardPosition) return false
        return x == other.x && y == other.y
    }
    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        return result
    }
}

fun BoardPosition.neighbour(direction: Direction): BoardPosition =
    when(direction){
        Direction.NORTH -> BoardPosition(x, y + 1)
        Direction.EAST -> BoardPosition(x + 1, y)
        Direction.SOUTH -> BoardPosition(x, y - 1)
        Direction.WEST -> BoardPosition(x - 1, y)
    }

fun String.toPosition(): BoardPosition{
    val (x, y) = this.split(",")
    return BoardPosition(x.toInt(), y.toInt())
}