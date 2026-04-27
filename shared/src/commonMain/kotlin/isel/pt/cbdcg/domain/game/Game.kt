package isel.pt.cbdcg.domain.game

data class Game(
    val id: UInt,
    val players: List<Player>,
    val board: Board = Board(),
    val turn: UInt
){

    fun placeTile(player: Player, position: BoardPosition, tile: Tile): Game{
        /*
        if(player.turn % turn != 0u)
            throw GameplayError.NotYourTurn()
        */
        return copy(board = board.place(position, tile), turn = turn + 1u)
    }

}