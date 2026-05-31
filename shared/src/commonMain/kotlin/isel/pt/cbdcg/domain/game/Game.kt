package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.MAX_TILES_IN_HAND
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.Entity
import isel.pt.cbdcg.domain.game.board.Tile
import isel.pt.cbdcg.domain.game.board.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.equipItem
import isel.pt.cbdcg.domain.game.board.placeCharacter
import isel.pt.cbdcg.domain.game.board.placeTile
import isel.pt.cbdcg.domain.game.board.reduceCooldown
import isel.pt.cbdcg.domain.game.board.toBoardTile
import isel.pt.cbdcg.domain.game.board.toBoardTileDTO
import isel.pt.cbdcg.domain.game.board.toTile
import isel.pt.cbdcg.domain.game.board.toTileDTO
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.toItem
import isel.pt.cbdcg.domain.game.character.toItemDTO
import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.dto.ItemDeckDTO
import isel.pt.cbdcg.dto.TileDeckDTO
import isel.pt.cbdcg.error.BoardError
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
    val turn: Turn,
    val winner: Player? = null
): Entity

fun Game.toGameDTO(): GameDTO {

    val playersDTO = players.map{ it.toPlayerDTO() }
    val spectatorsDTO = spectators.map{ it.toSpectatorDTO() }
    val boardDTO = board.tiles.map{ it.toBoardTileDTO() }
    val tileDeck = tileDeck.map{ (tile, copies) -> TileDeckDTO(tile.toTileDTO(), copies.toInt()) }.toTypedArray()
    val itemDeck = itemDeck.map{ (item, copies) -> ItemDeckDTO(item.toItemDTO(), copies.toInt()) }.toTypedArray()

    return GameDTO(
        id = id.toInt(),
        players = playersDTO.toTypedArray(),
        spectators = spectatorsDTO.toTypedArray(),
        board = boardDTO.toTypedArray(),
        tileDeck = tileDeck,
        itemDeck = itemDeck,
        turn = turn.toTurnDTO(),
        winner = winner?.toPlayerDTO()
    )
}

fun GameDTO.toGame(): Game {

    val players = players.map{ it.toPlayer() }
    val spectators = spectators.map{ it.toSpectator() }
    val tiles = board.map{ it.toBoardTile() }
    val tileDeck = tileDeck.associate { (tile, copies) -> tile.toTile() to copies.toUInt() }
    val itemDeck = itemDeck.associate { (item, copies) -> item.toItem() to copies.toUInt() }

    return Game(
        id = id.toUInt(),
        players = players,
        spectators = spectators,
        board = Board(tiles),
        tileDeck = tileDeck,
        itemDeck = itemDeck,
        turn = turn.toTurn(),
        winner = winner?.toPlayer()
    )

}

fun Game.placeOnBoard(player: Player, position: BoardPosition, card: Card, idx: UInt): Game {

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
fun Game.nextTurn(): Game {

    val remainingPlayers = turn.playerTurn.drop(1)

    val nextGameTurn =
        if(turn.gameTurn == 0u){
            val allTilesPlaced = players.all{ it.hand.numTileCards() == 0 }
            if(allTilesPlaced) 1u else 0u
        }
        else{ if (remainingPlayers.isEmpty()) turn.gameTurn + 1u else turn.gameTurn }

    val nextPlayerTurn = remainingPlayers.ifEmpty{ getTurnOrder() }
    val newBoard = if(remainingPlayers.isEmpty() && turn.gameTurn != 0u){ board.reduceCooldown() } else board

    val nextTurn = copy(
        board = newBoard,
        turn = Turn(gameTurn = nextGameTurn, playerTurn = nextPlayerTurn, phase = TurnPhase.CONSTRUCTION)
    )
    return nextTurn.startTurnDraw().checkWinner()
}
fun Game.checkWinner(): Game {

    val winningPlayer = players.firstOrNull { player ->
        val onStartTile = board.tiles.any {
            it.tile.specialEffect.type == TileEffectTypes.Start &&
            it.character != null && it.character.name == player.currentCharacter
        }

        onStartTile && player.hand.containsAllKeys()
    }

    return if (winningPlayer != null) copy(winner = winningPlayer)
    else this
}
fun Game.startTurnDraw(): Game {

    if (turn.gameTurn == 0u || tileDeck.values.all { it == 0u }) return this

    val nextPlayer = turn.playerTurn.first()

    val drawnTile = tileDeck.draw()
    val updatedDeck = tileDeck.remove(drawnTile)

    val updatedPlayers = players.map { player ->
        if (player.user.id == nextPlayer) player.addToHand(TileCard(drawnTile))
        else player
    }

    return copy(players = updatedPlayers, tileDeck = updatedDeck)
}
fun Game.leaveGame(player: Player): Game {

    val newPlayers = players.filter { it != player }
    if(newPlayers.size == 1) return copy(winner = newPlayers.first())

    val newBoard = board.tiles.map{ boardTile ->
        if(boardTile.character?.name == player.currentCharacter) boardTile.copy(character = null)
        else boardTile
    }

    val playerTiles = player.hand.filter{ (_, card) -> card.type == CardType.TILE }.map{ (it.value as TileCard).tile }
    val playerItems = player.hand.filter{ (_, card) -> card.type == CardType.ITEM }.map{ (it.value as ItemCard).item }
    val character = board.tiles.find{ it.character?.name == player.currentCharacter }?.character
    val characterItems = (character as? PlayableCharacter)?.items ?: emptyList()

    val newTileDeck = tileDeck.add(playerTiles)
    val newItemDeck = itemDeck.add(playerItems + characterItems)

    val newGame = copy(
        players = newPlayers,
        board = board.copy(tiles = newBoard),
        tileDeck = newTileDeck,
        itemDeck = newItemDeck
    )

    return if(turn.playerTurn.first() == player.user.id) newGame.nextTurn()
           else newGame
}

fun Game.moveCharacter(from: BoardTile, to: BoardTile) : Game {

    if(this.turn.phase != TurnPhase.MOVEMENT) throw GameError.CharacterMovementRestriction()
    if(to.character != null) throw BoardError.TileOccupied()

    val newBoard = board.tiles.map{ boardTile ->
        when (boardTile.pos) {
            from.pos -> boardTile.copy(character = null)
            to.pos -> boardTile.copy(character = from.character)
            else -> boardTile
        }
    }

    return copy(board = board.copy(tiles = newBoard)).checkWinner()
}
fun Game.drawItem(player: Player, boardTile: BoardTile): Game {

    if(player.user.id != turn.playerTurn.first())
        throw GameError.NotYourTurn()

    if(boardTile.cooldown != 0u)
        throw BoardError.EffectInCooldown(boardTile.tile.specialEffect.type.name, boardTile.cooldown.toInt())

    val newBoard = board.tiles.map{
        if(it == boardTile) boardTile.copy(cooldown = boardTile.tile.specialEffect.maxCooldown)
        else it
    }

    val items = when(boardTile.tile.specialEffect.type) {
        TileEffectTypes.Chest -> listOf(itemDeck.draw())

        TileEffectTypes.BigChest -> {
            val first = itemDeck.draw()
            val second = itemDeck.remove(first).draw()
            listOf(first, second)
        }

        else -> throw GameError.InvalidFormat("Draw Item", boardTile.tile.specialEffect.type.name)
    }

    val updatedPlayers = players.map{
        if(it == player) items.fold(player) { p, item -> p.addToHand(ItemCard(item)) }
        else it
    }

    val updatedItemDeck = items.fold(itemDeck) { deck, item -> deck.remove(item) }

    return copy(
        players = updatedPlayers,
        board = board.copy(tiles = newBoard),
        itemDeck = updatedItemDeck,
    )
}

private fun Game.getTurnOrder(): List<UInt>{
    return players.map{ it.user.id }
}