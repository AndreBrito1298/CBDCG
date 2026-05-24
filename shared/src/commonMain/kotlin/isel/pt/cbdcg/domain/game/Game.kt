package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.MAX_TILES_IN_HAND
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Effect
import isel.pt.cbdcg.domain.game.board.Entity
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.applyBoardTileEffect
import isel.pt.cbdcg.domain.game.board.equipItem
import isel.pt.cbdcg.domain.game.board.placeCharacter
import isel.pt.cbdcg.domain.game.board.placeTile
import isel.pt.cbdcg.domain.game.board.toBoardTile
import isel.pt.cbdcg.domain.game.board.toBoardTileDTO
import isel.pt.cbdcg.domain.game.board.toTile
import isel.pt.cbdcg.domain.game.board.toTileDTO
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.toItem
import isel.pt.cbdcg.domain.game.character.toItemDTO
import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.dto.ItemDeckDTO
import isel.pt.cbdcg.dto.TileDeckDTO
import isel.pt.cbdcg.error.GameError
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.ifEmpty

data class Game(
    val id: UInt,
    val players: List<Player>,
    val spectators: List<Spectator>,
    val board: Board = Board(),
    val tileDeck: Deck<Tile>,
    val itemDeck: Deck<Item>,
    val turn: Turn
): Entity

fun Game.toGameDTO(): GameDTO {

    val playersDTO = players.map{ it.toPlayerDTO() }
    val spectatorsDTO = spectators.map{ it.toSpectatorDTO() }
    val boardDTO = board.tiles.map{ it.toBoardTileDTO() }
    val tileDeck = tileDeck.map{ (tile, idx) -> TileDeckDTO(idx.toInt(), tile.toTileDTO()) }.toTypedArray()
    val itemDeck = itemDeck.map{ (item, idx) -> ItemDeckDTO(idx.toInt(), item.toItemDTO()) }.toTypedArray()

    return GameDTO(
        id = id.toInt(),
        players = playersDTO.toTypedArray(),
        spectators = spectatorsDTO.toTypedArray(),
        board = boardDTO.toTypedArray(),
        tileDeck = tileDeck,
        itemDeck = itemDeck,
        turn = turn.toTurnDTO()
    )
}

fun GameDTO.toGame(): Game {

    val players = players.map{ it.toPlayer() }
    val spectators = spectators.map{ it.toSpectator() }
    val tiles = board.map{ it.toBoardTile() }
    val tileDeck = tileDeck.associate { (idx, tile) -> tile.toTile() to idx.toUInt() }
    val itemDeck = itemDeck.associate { (idx, item) -> item.toItem() to idx.toUInt() }

    return Game(
        id = id.toUInt(),
        players = players,
        spectators = spectators,
        board = Board(tiles),
        tileDeck = tileDeck,
        itemDeck = itemDeck,
        turn = turn.toTurn()
    )

}

fun Game.applyBoardTileEffect(effect: Effect<BoardTile>, origin: BoardTile, vararg targets: BoardTile): Game {
    val result = effect.apply(origin, *targets)
    return copy(board = board.applyBoardTileEffect(result))
}
fun Game.placeOnBoard(player: Player, position: BoardPosition, card: Card, idx: UInt): Game{

    if(player.user.id != turn.playerTurn.first())
        throw GameError.NotYourTurn()

    if(turn.gameTurn == 0u && card.type != CardType.TILE)
        throw GameError.DungeonTurnZeroRule()

    val newBoard = when(card.type){
        CardType.TILE -> board.placeTile(position, (card as TileCard).tile, turn.phase)
        CardType.CHARACTER -> board.placeCharacter(position, player, (card as CharacterCard).character, turn.phase)
        CardType.ITEM -> board.equipItem(position, player, (card as ItemCard).item, turn.phase)
    }

    val updatedPlayers = players.map{
        if(it.user == player.user) player.removeFromHand(idx)
        else it
    }

    return copy(board = newBoard, players= updatedPlayers)
}
fun Game.nextPhase(): Game {

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
fun Game.nextTurn(): Game{

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
fun Game.startTurnDraw(): Game {

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
private fun Game.getTurnOrder(): List<UInt>{

    return players.map{ it.user.id }
}