package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.GameError

data class Game(
    val id: UInt,
    val players: List<Player>,
    val board: Board = Board(),
    val tileDeck: Map<Tile, UInt>,
    val turn: Turn
){

    fun nextTurn(): Turn{

        val list = turn.playerTurn.drop(1)

        return  if(list.isEmpty()) Turn(turn.gameTurn + 1u, getTurnOrder())
                else Turn(turn.gameTurn, list)
    }

    fun getTurnOrder(): List<UInt>{

        return players.map{ it.user }
    }
    fun placeTile(player: Player, position: BoardPosition, tile: Tile): Game{

        if(player.user != turn.playerTurn.first())
            throw GameError.NotYourTurn()

        val newBoard = board.place(position, tile)

        val players = players.map{
            if(it.user == player.user){
                val remove = player.hand.first{ it.connections.toSet() == tile.connections.toSet() }
                val hand = player.hand.toMutableList()
                hand.remove(remove)
                it.copy(hand = hand)
            } else it
        }

        return copy(board = newBoard, players= players, turn = nextTurn())
    }

}