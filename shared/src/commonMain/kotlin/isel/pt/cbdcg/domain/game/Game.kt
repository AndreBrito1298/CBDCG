package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.error.GameError

typealias TileDeck = Map<Tile, UInt>

fun TileDeck.draw(): Tile {

    val tiles = this.flatMap{ (tile, copies) -> List(copies.toInt()){ tile } }
    return tiles.random()
}
fun TileDeck.remove(removedTile: Tile): TileDeck =
    this.map{ (tile, copies) -> if(tile == removedTile) tile to copies - 1u else tile to copies }
        .toMap()

data class Game(
    val id: UInt,
    val players: List<Player>,
    val spectators: List<Spectator>,
    val board: Board = Board(),
    val tileDeck: TileDeck,
    val turn: Turn
){

    fun toGameDTO(): GameDTO {

        val playersDTO = players.map{ it.toPlayerDTO() }
        val spectatorsDTO = spectators.map{ it.toSpectatorInfo() }
        val boardDTO = board.tiles.map{ (pos, tile) -> "${pos.coords()}|${tile}" }
        val tileDeck = tileDeck.map{ (tile, nr) -> "${tile}|${nr}" }.toTypedArray()

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

        if(player.user.id != turn.playerTurn.first())
            throw GameError.NotYourTurn()

        val newBoard = board.place(position, tile)

        val updatedPlayers = players.map{
            if(it.user == player.user) player.removeFromHand(idx)
            else it
        }

        return copy(board = newBoard, players= updatedPlayers)
    }
    fun nextTurn(): Game{

        val list = turn.playerTurn.drop(1)

        val nextGameTurn =
            if(turn.gameTurn == 0u && players.any{ it.hand.numTileCards() == 0 }) 0u
            else turn.gameTurn + 1u

        return  if(list.isEmpty()) copy(turn =Turn(nextGameTurn, getTurnOrder()))
        else copy(turn = Turn(turn.gameTurn, list))
    }
    fun startTurnDraw(): Game {

        if(turn.gameTurn == 0u || tileDeck.values.all{ it == 0u }) return this

        val nextPlayer = turn.playerTurn.first()

        val drawnTile = tileDeck.draw()
        val updatedDeck = tileDeck.remove(drawnTile)

        val updatedPlayers = players.map{ player ->
            if(player.user.id == nextPlayer) player.addToHand(TileCard(drawnTile))
            else player
        }

        return copy(players = updatedPlayers, tileDeck = updatedDeck)
    }
    private fun getTurnOrder(): List<UInt>{

        return players.map{ it.user.id }
    }
}