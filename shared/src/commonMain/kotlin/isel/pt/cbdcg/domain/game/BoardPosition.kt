package isel.pt.cbdcg.domain.game

data class BoardPosition(val x: Int, val y: Int) {

    fun neighbour(direction: Direction): BoardPosition =
        when(direction){
            Direction.NORTH -> BoardPosition(x, y + 1)
            Direction.EAST -> BoardPosition(x + 1, y)
            Direction.SOUTH -> BoardPosition(x, y - 1)
            Direction.WEST -> BoardPosition(x - 1, y)
        }

}

