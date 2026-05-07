package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.error.GameError

data class Game(
    val id: UInt,
    val players: List<Player>,
    val spectators: List<Spectator>,
    val board: Board = Board(),
    val tileDeck: Map<Tile, UInt>,
    val turn: Turn
){

    fun toGameDTO(): GameDTO {

        val playersDTO = players.map{ it.toPlayerInfo() }
        val spectatorsDTO = spectators.map{ it.toSpectatorInfo() }
        val boardDTO = board.tiles.map{ (pos, tile) -> "${pos.coords()}|${tile.codeString()}" }
        val tileDeck = tileDeck.map{ (tile, nr) -> "${tile.codeString()}|${nr}" }.toTypedArray()

        return GameDTO(
            id = id.toInt(),
            players = playersDTO.toTypedArray(),
            spectators = spectatorsDTO.toTypedArray(),
            board = boardDTO.toTypedArray(),
            tileDeck = tileDeck,
            turn = turn.turnString()
        )
    }


    fun placeTile(player: Player, position: BoardPosition, tile: Tile, idx: UInt): Game{

        if(player.user != turn.playerTurn.first())
            throw GameError.NotYourTurn()

        val newBoard = board.place(position, tile)

        val updatedPlayers = players.map{
            if(it.user == player.user) player.removeFromHand(idx)
            else it
        }

        return copy(board = newBoard, players= updatedPlayers)
    }


    // memória persistente (biblioteca KMP chave-valor)
    fun nextTurn(): Game{

        val list = turn.playerTurn.drop(1)

        val nextGameTurn =
            if(turn.gameTurn == 0u && players.any{ it.hand.isNotEmpty() }) 0u
            else turn.gameTurn + 1u

        return  if(list.isEmpty()) copy(turn =Turn(nextGameTurn, getTurnOrder()))
        else copy(turn = Turn(turn.gameTurn, list))
    }
    fun startTurnDraw(): Game {

        if(turn.gameTurn == 0u) return this

        val nextPlayer = turn.playerTurn.first()

        val available = tileDeck.filterValues { it > 0u }.keys.toList()
        val drawnTile = available.random()

        val updatedPlayers = players.map{ player ->
            if(player.user == nextPlayer) player.addToHand(drawnTile)
            else player
        }
        val remainingTiles = tileDeck.map{ (tile, nr) ->
            if(tile == drawnTile) tile to nr - 1u else tile to nr
        }.toMap()

        return copy(players = updatedPlayers, tileDeck = remainingTiles)
    }
    private fun getTurnOrder(): List<UInt>{

        return players.map{ it.user }
    }
}