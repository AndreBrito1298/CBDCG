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
import isel.pt.cbdcg.domain.game.board.unequip
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.character.toItem
import isel.pt.cbdcg.domain.game.character.toItemDTO
import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.dto.ItemDeckDTO
import isel.pt.cbdcg.dto.TileDeckDTO
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.CharacterError
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
): Entity {
    override fun applyToGame(game: Game): Game {
        TODO("Not yet implemented")
    }
}

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

fun Game.resolveTurnZero() : Game {

    val remainingPlayers = turn.playerTurn.drop(1)
    val nextPlayerTurn = remainingPlayers.ifEmpty { getTurnOrder() }
    
    val allTilesPlaced = players.all{ it.hand.numTileCards() == 0 }
    val allPlayersPlacedACharacter = players.map{ player ->
        player to board.tiles.find{ it.character != null && it.character.name == player.currentCharacter }?.character
    }.all{ it.second != null }
    
    return if(!allTilesPlaced) copy(turn = Turn(0u, nextPlayerTurn, TurnPhase.CONSTRUCTION))
           else if(allTilesPlaced && !allPlayersPlacedACharacter) copy(turn = Turn(0u, nextPlayerTurn, TurnPhase.SUBSTITUTION))
           else copy(turn = Turn(1u, nextPlayerTurn, TurnPhase.CONSTRUCTION)).startTurnDraw()
    
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

    val nextGameTurn = if (remainingPlayers.isEmpty()) turn.gameTurn + 1u else turn.gameTurn
    val nextPlayerTurn = remainingPlayers.ifEmpty { getTurnOrder() }
    val newBoard = if (remainingPlayers.isEmpty()) board.reduceCooldown() else board

    val nextTurn = copy(
        board = newBoard,
        turn = Turn(gameTurn = nextGameTurn, playerTurn = nextPlayerTurn, phase = TurnPhase.CONSTRUCTION)
    )
    return nextTurn.startTurnDraw()
}

private fun Game.getTurnOrder(): List<UInt>{
    
    val playerToCharacter = players.map{ player ->
        player to board.tiles.find{ it.character != null && it.character.name == player.currentCharacter }?.character
    }
    
    val allPlayersPlacedACharacter = playerToCharacter.all { it.second != null }
    
    return if (allPlayersPlacedACharacter) {
        playerToCharacter
            .sortedByDescending { it.second!!.adjustStats().spe }
            .map { it.first.user.id }
    } else {
        players.map { it.user.id }
    }
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

// Up from here will eventually be replaced with the polymorphic structure

fun Game.placeOnBoard(player: Player, position: BoardPosition, card: Card, idx: UInt): Game {

    if(player.user.id != turn.playerTurn.first())
        throw GameError.NotYourTurn()

    if(turn.gameTurn == 0u && turn.phase == TurnPhase.CONSTRUCTION && card.type != CardType.TILE)
        throw GameError.DungeonTurnZeroRule()

    return when(card.type){
        
        CardType.TILE -> placeTile(player, position, card as TileCard, idx)
        CardType.CHARACTER -> placeCharacter(player, position, card as CharacterCard, idx)
        CardType.ITEM -> placeItem(player, position, card as ItemCard, idx)
    }
}
fun Game.placeTile(player: Player, position: BoardPosition, card: TileCard, idx: UInt): Game {

    if(turn.phase != TurnPhase.CONSTRUCTION)
        throw GameError.TilePlacementRestriction()

    val updatedPlayers = players.map{
        if(it.user == player.user) player.removeFromHand(idx)
        else it
    }
    
    val updatedBoard = board.placeTile(position, card.tile)
    return copy(board = updatedBoard, players = updatedPlayers)
}
fun Game.placeCharacter(player: Player, position: BoardPosition, card: CharacterCard, idx: UInt): Game {

    if(turn.phase != TurnPhase.SUBSTITUTION)
        throw GameError.CharacterPlacementRestriction()

    val playerWithoutCard = player.removeFromHand(idx)
    val characterInTile = board.tiles.find{ it.pos == position }?.character

    return if((characterInTile as? PlayableCharacter) == null || characterInTile.name != player.currentCharacter){

        if(player.currentCharacter != null)
            throw BoardError.CharacterLimitReached()

        val updatedPlayers = players.map{
            if(it.user == player.user) playerWithoutCard
            else it
        }

        copy(board = board.placeCharacter(position, card.character), players = updatedPlayers)

    } else {

        val characterItemCards = characterInTile.items.map{ item -> ItemCard(item) }
        val characterCard = CharacterCard(characterInTile.copy(items = emptyList()))

        val updatedPlayer = (characterItemCards + characterCard)
                .fold(playerWithoutCard){ initial, card -> initial.addToHand(card) }

        val updatedBoard = board
            .copy(tiles = board.tiles.map{ if(it.pos == position) it.copy(character = null) else it })
            .placeCharacter(position, card.character)

        val updatedPlayers = players.map{
            if(it.user == player.user) updatedPlayer
            else it
        }

        copy(board = updatedBoard, players = updatedPlayers)
    }
}
fun Game.placeItem(player: Player, position: BoardPosition, card: ItemCard, idx: UInt): Game {

    if(turn.phase != TurnPhase.SUBSTITUTION)
        throw GameError.EquipItemRestriction()

    val updatedPlayers = players.map{
        if(it.user == player.user) player.removeFromHand(idx)
        else it
    }

    val updatedBoard = board.equipItem(position, player, card.item)
    return copy(board = updatedBoard, players = updatedPlayers)
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
        throw BoardError.EffectInCooldown(boardTile.tile.specialEffect.type.name, boardTile.cooldown!!.toInt())

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

fun Game.unequip(player: Player, character: Character, idx: Int): Game {

    if(this.turn.phase != TurnPhase.SUBSTITUTION)
        throw GameError.EquipItemRestriction()

    if(character !is PlayableCharacter || character.name != player.currentCharacter)
        throw BoardError.EquipYourCharacter()

    val item = character.items.getOrNull(idx)
        ?: throw CharacterError.ItemDoesNotExist(idx)

    val updatedBoard = board.unequip(character, item)
    val updatedPlayers = players.map{
        if(it.user == player.user) it.addToHand(ItemCard(item))
        else it
    }

    return copy(board = updatedBoard, players = updatedPlayers)
}
