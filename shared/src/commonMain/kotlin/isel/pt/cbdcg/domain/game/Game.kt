package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.error.GameError

data class Game(
    val id: UInt,
    val players: List<Player>,
    val board: Board = Board(),
    val tileDeck: Map<Tile, UInt>,
    val turn: Turn
){
    fun placeTile(player: Player, position: BoardPosition, tile: Tile, idx: UInt): Game{

        if(player.user != turn.playerTurn.first())
            throw GameError.NotYourTurn()

        val newBoard = board.place(position, tile)

        val players = players.map{
            if(it.user == player.user){
                val updatedHand = player.hand
                    .filterKeys{ it != idx }.values
                    .mapIndexed{ newIdx, tile -> newIdx.toUInt() to tile }
                    .toMap()
                player.copy(hand = updatedHand)
            } else it
        }

        return copy(board = newBoard, players= players)
    }

    fun resolveState(): Game {

        val nextTurn = nextTurn()
        val nextPlayer = nextTurn.playerTurn.first()

        return  if(nextTurn.gameTurn != 0u) {

                    val available = tileDeck.filterValues { it > 0u }.keys.toList()
                    val drawnTile = available.random()

                    val updatedPlayers = players.map{ player ->
                        if(player.user == nextPlayer) {
                            val lastKey = player.hand.keys.lastOrNull() ?: 0u
                            val updatedHand = player.hand.plus(lastKey to drawnTile)
                            player.copy(hand = updatedHand)
                        } else player
                    }

                    val remainingTiles = tileDeck.map{ (tile, nr) ->
                        if(tile == drawnTile) tile to nr - 1u else tile to nr
                    }.toMap()

                    copy(tileDeck = remainingTiles, players = updatedPlayers, turn = nextTurn)

                } else {
                    copy(turn = nextTurn)
                }
    }
    fun nextTurn(): Turn{

        val list = turn.playerTurn.drop(1)

        val nextGameTurn =
            if(turn.gameTurn == 0u && players.any{ it.hand.isNotEmpty() }) 0u
            else turn.gameTurn + 1u

        return  if(list.isEmpty()) Turn(nextGameTurn, getTurnOrder())
        else Turn(turn.gameTurn, list)
    }
    fun getTurnOrder(): List<UInt>{

        return players.map{ it.user }
    }
}