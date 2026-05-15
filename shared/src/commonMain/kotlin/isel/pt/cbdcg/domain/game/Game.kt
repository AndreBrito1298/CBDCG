package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Effect
import isel.pt.cbdcg.domain.game.board.EffectResult
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.character.Character
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
    fun applyBoardTileEffect(effect: Effect<BoardTile>, origin: BoardTile, vararg targets: BoardTile): Game {
        val result = effect.apply(origin, *targets)
        return copy(board = board.applyBoardTileEffect(result))
    }

    private val MAX_TILES_IN_HAND = 3


    fun toGameDTO(): GameDTO {

        val playersDTO = players.map{ it.toPlayerDTO() }
        val spectatorsDTO = spectators.map{ it.toSpectatorDTO() }
        val boardDTO = board.tiles.map{ it.toBoardTileDTO() }
        val tileDeck = tileDeck.map{ (tile, nr) -> "${tile}|${nr}" }.toTypedArray()

        return GameDTO(
            id = id.toInt(),
            players = playersDTO.toTypedArray(),
            spectators = spectatorsDTO.toTypedArray(),
            board = boardDTO.toTypedArray(),
            tileDeck = tileDeck,
            turn = turn.toString()
        )
    }


    fun placeOnBoard(player: Player, position: BoardPosition, card: Card, idx: UInt): Game{

        if(player.user.id != turn.playerTurn.first())
            throw GameError.NotYourTurn()

        if(turn.gameTurn == 0u && card.type != CardType.TILE)
            throw GameError.DungeonTurnZeroRule()

        val newBoard = board.place(position, card, turn.phase)

        val updatedPlayers = players.map{
            if(it.user == player.user) player.removeFromHand(idx)
            else it
        }

        return copy(board = newBoard, players= updatedPlayers)
    }
    fun nextPhase(): Game {

        val player = players.find{ it.user.id == turn.playerTurn.first() }
            ?: throw GameError.NotYourTurn()

        return when(turn.phase){
            TurnPhase.CONSTRUCTION -> {
                val nrTiles = player.hand.numTileCards()
                if(nrTiles > MAX_TILES_IN_HAND)
                    throw GameError.MustPlaceTile(MAX_TILES_IN_HAND)

                copy(turn = Turn(turn.gameTurn, turn.playerTurn, TurnPhase.SUBSTITUTION))
            }
            TurnPhase.SUBSTITUTION -> {
                player.currentCharacter ?: throw GameError.NoActiveCharacters()

                copy(turn = Turn(turn.gameTurn, turn.playerTurn, TurnPhase.MOVEMENT))
            }
            TurnPhase.MOVEMENT -> nextTurn()
        }
    }
    fun nextTurn(): Game{

        val remainingPlayers = turn.playerTurn.drop(1)

        val nextGameTurn =
            if(turn.gameTurn == 0u){
                val allTilesPlaced = players.all{ it.hand.numTileCards() == 0 }
                if(allTilesPlaced) 1u else 0u
            }
            else{ if (remainingPlayers.isEmpty()) turn.gameTurn + 1u else turn.gameTurn }

        val nextPlayerTurn =
            remainingPlayers.ifEmpty { getTurnOrder() }

        val nextTurn = copy(turn = Turn(gameTurn = nextGameTurn, playerTurn = nextPlayerTurn, phase = TurnPhase.CONSTRUCTION))
        return nextTurn.startTurnDraw()
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
