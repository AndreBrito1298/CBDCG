package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.BATTLE_TURN_DURATION_SECONDS
import isel.pt.cbdcg.MAX_TILES_IN_HAND
import isel.pt.cbdcg.REMAINING_SECONDS_AFTER_BATTLE
import isel.pt.cbdcg.TURN_DURATION_SECONDS
import isel.pt.cbdcg.domain.game.board.Board
import isel.pt.cbdcg.domain.game.board.BoardPosition
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.checkBlocked
import isel.pt.cbdcg.domain.game.board.connectedNeighbours
import isel.pt.cbdcg.domain.game.board.tile.Tile
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.equipItem
import isel.pt.cbdcg.domain.game.board.tile.getStatModifier
import isel.pt.cbdcg.domain.game.board.connectionDistancesFrom
import isel.pt.cbdcg.domain.game.board.placeCharacter
import isel.pt.cbdcg.domain.game.board.placeTile
import isel.pt.cbdcg.domain.game.board.reduceCooldown
import isel.pt.cbdcg.domain.game.board.tile.isPositive
import isel.pt.cbdcg.domain.game.board.toBoardTile
import isel.pt.cbdcg.domain.game.board.toBoardTileDTO
import isel.pt.cbdcg.domain.game.board.tile.toTile
import isel.pt.cbdcg.domain.game.board.tile.toTileDTO
import isel.pt.cbdcg.domain.game.board.unequip
import isel.pt.cbdcg.domain.game.board.possibleUnoccupiedPositions
import isel.pt.cbdcg.domain.game.board.tile.allRotations
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.ItemEvolution
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.character.special
import isel.pt.cbdcg.domain.game.character.toItem
import isel.pt.cbdcg.domain.game.character.toItemDTO
import isel.pt.cbdcg.dto.EntityDTO
import isel.pt.cbdcg.dto.GameDTO
import isel.pt.cbdcg.dto.ItemDeckDTO
import isel.pt.cbdcg.dto.TileDeckDTO
import isel.pt.cbdcg.error.BattleError
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.CharacterError
import isel.pt.cbdcg.error.GameError
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.contains
import kotlin.collections.ifEmpty

data class Game(
    val id: UInt,
    val players: List<Player>,
    val spectators: List<Spectator>,
    val board: Board = Board(),
    val tileDeck: Deck<Tile>,
    val itemDeck: Deck<Item>,
    val turn: Turn,
    val battle : Battle? = null,
): Entity {
    override fun Entity.toEntityDTO(): EntityDTO {
        return EntityDTO()
    }

    override fun <T : Entity> toEntity() = this as Entity
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
        battle = battle?.toBattleDTO(),
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
        battle = battle?.toBattle(),
    )

}

fun Game.resolveTurnZero() : Game {

    val remainingPlayers = turn.playerTurn.drop(1)
    val nextPlayerTurn = remainingPlayers.ifEmpty { getTurnOrder() }
    
    val allTilesPlaced = players.all{ it.hand.numTileCards() == 0 }
    val allPlayersPlacedACharacter = players.map{ player ->
        player to board.tiles.find{ it.character != null && it.character.name == player.currentCharacter }?.character
    }.all{ it.second != null }
    
    return  if(!allTilesPlaced)
                copy(
                    turn = turn.copy(
                        playerTurn = nextPlayerTurn,
                        phase = TurnPhase.CONSTRUCTION,
                        deadline = newDeadline(TURN_DURATION_SECONDS)
                    )
                )
            else if(allTilesPlaced && !allPlayersPlacedACharacter)
                copy(
                    turn = turn.copy(
                        playerTurn = nextPlayerTurn,
                        phase = TurnPhase.SUBSTITUTION,
                        deadline = newDeadline(TURN_DURATION_SECONDS)
                    )
                )
            else {
                val newGameTurn = copy(turn = turn.copy(gameTurn = 1u, playerTurn = emptyList(), phase = TurnPhase.CONSTRUCTION))
                val turnOrder = newGameTurn.getTurnOrder()

                newGameTurn.copy(turn = newGameTurn.turn.copy(playerTurn = turnOrder)).startNextTurn()
            }
}
fun Game.nextPhase(): Game {

    val player = players.find{ it.user.id == turn.playerTurn.first() }
        ?: throw GameError.NotYourTurn()

    return when(turn.phase){
        TurnPhase.CONSTRUCTION -> {
            val nrTiles = player.hand.numTileCards()
            if(nrTiles > MAX_TILES_IN_HAND)
                throw GameError.MustPlaceTile(MAX_TILES_IN_HAND)

            copy(turn = turn.copy(phase = TurnPhase.SUBSTITUTION, deadline = turn.extendDeadline(5)))
        }
        TurnPhase.SUBSTITUTION -> {
            player.currentCharacter ?: throw GameError.NoActiveCharacters()

            copy(turn = turn.copy(phase = TurnPhase.MOVEMENT, deadline = turn.extendDeadline(5)))
        }
        TurnPhase.MOVEMENT -> nextTurn()
    }
}
fun Game.nextTurn(): Game {

    val winner = checkWinner()
    if(winner.players.size == 1) return this

    val remainingPlayers = turn.playerTurn.drop(1)

    val nextGameTurn = if (remainingPlayers.isEmpty()) turn.gameTurn + 1u else turn.gameTurn
    val nextPlayerTurn = remainingPlayers.ifEmpty { getTurnOrder() }
    val newBoard = if (remainingPlayers.isEmpty()) board.reduceCooldown() else board

    val nextTurn = copy(
        board = newBoard,
        turn = turn.copy(gameTurn = nextGameTurn, playerTurn = nextPlayerTurn)
    )
    return nextTurn.startNextTurn()
}
private fun Game.getTurnOrder(): List<UInt>{
    
    val playerToCharacter = players.map{ player ->
        player to board.tiles.find{ it.character != null && it.character.name == player.currentCharacter }?.character
    }

    return if (turn.gameTurn > 0u) {
        playerToCharacter
            .sortedByDescending { it.second?.adjustStats()?.spe ?: 0 }
            .map { it.first.user.id }
    } else {
        players.map { it.user.id }
    }
}
private fun Game.checkWinner(): Game =
    players.fold(this) { currentGame, player ->

        val hasCharactersRemaining =
            player.hand.numCharacterCards() > 0 ||
            currentGame.board.tiles.any {
                it.character != null &&
                it.character.name == player.currentCharacter &&
                it.character.adjustStats().hp > 0
            }

        if (!hasCharactersRemaining) currentGame.leaveGame(player, true)
        else {

            if (!player.hand.containsAllKeys()) currentGame
            else {

                val characterOnStart = currentGame.board.tiles
                    .filter { it.tile.specialEffect == TileEffectTypes.Start }
                    .any { it.character != null && it.character.name == player.currentCharacter }

                if (!characterOnStart) currentGame
                else {
                    val otherPlayers = players.filter { it.user.id != player.user.id }
                    return currentGame.copy(
                        players = listOf(player),
                        spectators = otherPlayers.map { Spectator(it.user) }
                    )
                }
            }

        }
    }
fun Game.startNextTurn(): Game {

    if (turn.gameTurn == 0u) return this

    val anyRemainingTilesInDeck = tileDeck.values.any{ it > 0u }

    val nextPlayerId = turn.playerTurn.first()

    val drawnTile = if(anyRemainingTilesInDeck) tileDeck.draw() else null
    val updatedDeck = if(drawnTile != null) tileDeck.remove(drawnTile) else tileDeck

    val updatedPlayers = players.map { player ->
        if (player.user.id == nextPlayerId && player.hand.numTileCards() < MAX_TILES_IN_HAND && drawnTile != null)
            player.addToHand(TileCard(drawnTile))
        else player
    }

    val player = players.find{ player -> player.user.id == nextPlayerId }
        ?: throw GameError.PlayerNotFound(nextPlayerId.toInt(), null, id.toInt())

    val updatedCharacterModifiers = board.copy(tiles =
        board.tiles.map{ boardTile ->
            val character = boardTile.character
            if(character != null && player.currentCharacter == character.name)
                boardTile.copy(character = character.decreaseTileEffectModifiers())
            else boardTile
        }
    )

    val availableTilePositions = board.possibleUnoccupiedPositions()
    val nextPhase = if(availableTilePositions.isNotEmpty()) TurnPhase.CONSTRUCTION else TurnPhase.SUBSTITUTION

    return copy(
        players = updatedPlayers,
        tileDeck = updatedDeck,
        board = updatedCharacterModifiers,
        turn = turn.copy(phase = nextPhase, deadline = newDeadline(TURN_DURATION_SECONDS))
    )
}
fun Game.leaveGame(player: Player, toSpectator: Boolean = false): Game {

    val newPlayers = players.filter { it.user.id != player.user.id }
    if(newPlayers.size == 1) return copy(
        players = newPlayers,
        spectators = if(toSpectator) spectators + Spectator(player.user) else spectators,
    )

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
        spectators = if(toSpectator) spectators + Spectator(player.user) else spectators,
        board = board.copy(tiles = newBoard),
        tileDeck = newTileDeck,
        itemDeck = newItemDeck
    )

    return if(turn.playerTurn.first() == player.user.id) newGame.nextTurn()
           else newGame
}
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

    return if(characterInTile == null || characterInTile.name != player.currentCharacter){

        if(player.currentCharacter != null)
            throw BoardError.CharacterLimitReached()

        val updatedPlayers = players.map{
            if(it.user == player.user) playerWithoutCard
            else it
        }

        copy(board = board.placeCharacter(position, card.character), players = updatedPlayers)

    } else {

        if(characterInTile.name != player.currentCharacter)
            throw GameError.ReplaceYourCharacter()

        val characterItemCards = (characterInTile as PlayableCharacter).items.map{ item -> ItemCard(item) }
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
fun Game.unequip(player: Player, character: Character, idx: Int): Game {

    if(this.turn.phase != TurnPhase.SUBSTITUTION)
        throw GameError.EquipItemRestriction()

    if(character !is PlayableCharacter || character.name != player.currentCharacter)
        throw BoardError.ApplyEffectOnYourCharacter()

    val item = character.items.getOrNull(idx)
        ?: throw CharacterError.ItemDoesNotExist(idx)

    val updatedBoard = board.unequip(character, item)
    val updatedPlayers = players.map{
        if(it.user == player.user) it.addToHand(ItemCard(item))
        else it
    }

    return copy(board = updatedBoard, players = updatedPlayers)
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
        TileEffectTypes.Chest -> listOf(itemDeck.filter{ (item, _) -> !item.grade.special() }.draw())

        TileEffectTypes.BigChest -> {

            val commonItem = itemDeck
                .filter{ (item, _) -> !item.grade.special() }
                .draw()

            val charactersOnBoard = board.tiles.mapNotNull{ it.character }
            val evolveItems = charactersOnBoard
                .filter{ it.evolution != null && it.evolution is ItemEvolution }
                .map{ (it.evolution as ItemEvolution).item }

            val specialItem = itemDeck
                .filter{ (item, _) -> item.grade.special() && (item.grade == Grade.KEY || item.name in evolveItems) }
                .draw()

            listOf(commonItem, specialItem)
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
fun Game.updateStatModifiers(player: Player, boardTile: BoardTile): Game {

    if(player.user.id != turn.playerTurn.first())
        throw GameError.NotYourTurn()

    if(boardTile.cooldown != 0u)
        throw BoardError.EffectInCooldown(boardTile.tile.specialEffect.type.name, boardTile.cooldown!!.toInt())

    val character = (boardTile.character as? PlayableCharacter)
        ?: throw BoardError.EmptyTile()

    if(character.name != player.currentCharacter)
        throw BoardError.ApplyEffectOnYourCharacter()

    val specialEffect = boardTile.tile.specialEffect
    val statModifier = specialEffect.getStatModifier()
    val range = specialEffect.range.toInt()

    val distances = board.connectionDistancesFrom(boardTile)
    val affectedTiles =
        if(specialEffect.isPositive()) distances.filter { it.value <= range }.keys
        else distances.filter{ it.value in 1..range }.keys

    val newBoard = board.tiles.map{ tile ->

        val isOrigin = tile.pos == boardTile.pos
        val affectedTile = affectedTiles.find { it.pos == tile.pos }

        val newCooldown = if(isOrigin) specialEffect.maxCooldown else tile.cooldown

        val updatedCharacter = if(affectedTile != null && tile.character is PlayableCharacter) {
            tile.character.copy(activeStatModifiers = tile.character.activeStatModifiers + statModifier)
        } else tile.character

        tile.copy(
            cooldown = newCooldown,
            character = updatedCharacter
        )
    }

    return copy(
        board = board.copy(tiles = newBoard)
    )
}
fun Game.battle(attacker: Character, defender: Character): Game {

    if(battle != null) throw GameError.BattleNotConcluded()
    if(turn.phase != TurnPhase.MOVEMENT)
        throw GameError.MoveToBattleRestriction()

    val mainCharacters = listOf(attacker, defender)
    val mainPlayers = players.filter{
        it.currentCharacter in mainCharacters.map{ character -> character.name }
    }
    val mainCharactersReady = mainCharacters.map{ character ->
        BattleAction(
            origin = character,
            target = null,
            action = PossibleBattleActions.FLEE,
            stats = Stats()
        )
    }
    val itemBet = mainPlayers.map{ player ->

        val itemCards = player.hand.mapNotNull{ card -> (card.value as? ItemCard) }
        val keyItems = itemCards.filter{ itemCard -> itemCard.item.grade == Grade.KEY }

        if(keyItems.isNotEmpty()) BattleBet(player, keyItems.random().item)
        else if(itemCards.isNotEmpty()) BattleBet(player, itemCards.random().item)
        else BattleBet(player, null)
    }

    val participatingCharacterTiles = board.tiles.filter{ boardTile ->
        val characterInTile = boardTile.character
        characterInTile != null && mainCharacters.any{ it.name == characterInTile.name }
    }


    val adjacentCharacters = participatingCharacterTiles
        .flatMap{ boardTile -> board.connectedNeighbours(boardTile) }
        .mapNotNull{ neighbour -> neighbour.character }
        .filter{ character -> character.name !in mainCharacters.map{ it.name } }
        .distinct()


    val charactersInBattle = mainCharacters + adjacentCharacters

    return copy(
        battle =
            Battle(
                phase = BattlePhase.WAITING,
                characters = charactersInBattle,
                itemBet = itemBet,
                pending = mainCharactersReady
            ),
        turn = turn.copy(deadline = newDeadline(BATTLE_TURN_DURATION_SECONDS))
    )
}
fun Game.joinBattle(player: Player, character: Character): Game {

    if(battle == null) throw GameError.NoBattleOngoing()

    val ready = BattleAction(
        origin = character,
        target = null,
        action = PossibleBattleActions.FLEE,
        stats = Stats()
    )

    val itemCards = player.hand.mapNotNull{ card -> (card.value as? ItemCard) }
    val keyItems = itemCards.filter{ itemCard -> itemCard.item.grade == Grade.KEY }

    val bet = if(keyItems.isNotEmpty()) BattleBet(player, keyItems.random().item)
              else if(itemCards.isNotEmpty()) BattleBet(player, itemCards.random().item)
              else BattleBet(player, null)

    return copy(
        battle = battle.copy(
            pending = battle.pending + ready,
            itemBet = battle.itemBet + bet
        )
    )
}
fun Game.leaveBattle(character: Character): Game {

    if(battle == null) throw GameError.NoBattleOngoing()

    val idx = battle.characters.indexOf(character)
    if(idx == 0 || idx == 1) throw BattleError.CantLeaveBattle()

    return copy(battle = battle.copy(characters = battle.characters - character))
}
fun Game.addActionToPending(origin: Character, target: Character?, action: PossibleBattleActions): Game {

    if(battle == null)
        throw GameError.NoBattleOngoing()

    if(battle.pending.any{ it.origin.name == origin.name })
        throw BattleError.ActionAlreadyQueued()

    val battleAction =
        BattleAction(
            origin = origin,
            target = target,
            action = action,
            stats = Stats()
        )

    return copy(battle = battle.copy(pending = battle.pending + battleAction))
}
fun Game.removeActionFromPending(character: Character): Game {

    if(battle == null)
        throw GameError.NoBattleOngoing()

    val action = battle.pending.find{ it.origin == character }
        ?: throw BattleError.ActionNotQueued()

    return copy(battle = battle.copy(pending = battle.pending - action))
}
fun Game.resolvePending(): Game {

    if(battle == null)
        throw GameError.NoBattleOngoing()

    val availableCharacters = battle.characters
        .filter{ it.adjustStats().hp > 0 }
        .sortedByDescending { it.adjustStats().spe }

    if(battle.pending.size < availableCharacters.size) return this
    if(battle.currentTurn == 0u)
        return copy(battle = battle.copy(phase = BattlePhase.BATTLING, pending = emptyList(), currentTurn = 1u))

    val orderOfActions: List<BattleAction> = availableCharacters.map{ character ->
        val action = battle.pending.find{ it.origin == character }
        requireNotNull(action) { "Action not found for character ${character.name}" }
    }

    val updatedBattle = orderOfActions.fold<BattleAction, Battle>(battle.incrementModifiers()){ currentBattle, pending ->
        val character = currentBattle.characters.find{ it.name == pending.origin.name }

        if(character != null && character.adjustStats().hp > 0){
            when(pending.action){
                PossibleBattleActions.HOLD -> currentBattle.hold(pending)
                PossibleBattleActions.FLEE -> currentBattle.flee(pending)
                PossibleBattleActions.ATTACK -> {
                    val target = currentBattle.characters.find{ it.name == pending.target?.name }
                    if(target == null)
                        throw BattleError.CharacterNotFound(pending.target?.name ?: "")

                    if(target.adjustStats().hp > 0) currentBattle.attack(pending)
                    else currentBattle
                }
            }
        } else currentBattle
    }

    val winner = updatedBattle.characters.filter{ it.adjustStats().hp > 0 }

    return  if(winner.size == 1) resolveBattleEnd(updatedBattle, winner.first())
            else copy(
                battle = updatedBattle.copy(pending = emptyList(), currentTurn = battle.currentTurn + 1u),
                turn = turn.copy(deadline = newDeadline(BATTLE_TURN_DURATION_SECONDS))
            )
}
fun Game.resolveBattleEnd(battle: Battle, winner: Character): Game {

    val fled = battle.characters.filter{ character ->
        character.activeStatModifiers.any{ mod -> mod.type == ModifierType.BATTLE_FLEE && mod.stats.hp != 0 }
    }.map{ it.name }

    val updatedBoardTiles = board.tiles.map{ boardTile ->
        val character = boardTile.character
        if(character != null && battle.characters.any{ it.name == character.name } ){

            val defaultCharacter = character.removeAllBattleMods()
            val newCharacter =
                if(character.name != winner.name && character.name !in fled)
                    defaultCharacter.addModifier(
                        newStatModifier = StatModifier(
                            stats = Stats(-1,0,0,0),
                            duration = 0u,
                            type = ModifierType.PERMANENT
                        )
                    )
                else defaultCharacter

            boardTile.copy(character = newCharacter)
        } else boardTile
    }

    val updatedPlayers = players.map{ player ->
        if(player.currentCharacter == winner.name)
            battle.itemBet.fold(player){ currentPlayer, bet ->
                val item = bet.item
                if(bet.player != player && item != null) currentPlayer.addToHand(ItemCard(item))
                else currentPlayer
            }
        else {
            val item = battle.itemBet.find{ it.player == player }?.item
            if(item == null) return@map player

            val itemIdx = player.hand.filterValues { (it as? ItemCard)?.item == item }.keys.firstOrNull()
            if(itemIdx == null) return@map player

            player.removeFromHand(itemIdx)
        }
    }

    return copy(
        battle = battle.copy(phase = BattlePhase.ENDING, pending = emptyList()),
        players = updatedPlayers,
        board = board.copy(tiles = updatedBoardTiles),
        turn = turn.copy(deadline = newDeadline(BATTLE_TURN_DURATION_SECONDS))
    )
}
fun Game.deleteBattle(): Game{

    if(battle == null)
        throw GameError.NoBattleOngoing()

    if(battle.pending.size < battle.characters.size)
        return this

    val charactersAfterBattle = players.associateWith { player ->
        val character = board.tiles.find{ it.character?.name == player.currentCharacter }?.character
            ?: throw BattleError.CharacterNotFound(player.currentCharacter ?: "")
        character.evolve(battle)
    }

    val updatedPlayers = players.map{ player ->
        val player = charactersAfterBattle.keys.find{ it.user.id == player.user.id } ?: return@map player
        val character = charactersAfterBattle[player] ?: return@map player

        if(character.adjustStats().hp > 0) player.copy(currentCharacter = character.name)
        else player.copy(currentCharacter = null)
    }

    val updatedTiles = board.tiles.map{ boardTile ->
        val character = boardTile.character ?: return@map boardTile
        val player = players.find{ it.currentCharacter == character.name } ?: return@map boardTile
        val characterAfterBattle = charactersAfterBattle[player] ?: return@map boardTile

        if(characterAfterBattle.adjustStats().hp > 0) boardTile.copy(character = characterAfterBattle)
        else boardTile.copy(character = null)

    }

    return copy(
        battle = null,
        players = updatedPlayers,
        board = board.copy(tiles = updatedTiles),
        turn = turn.copy(deadline = newDeadline(REMAINING_SECONDS_AFTER_BATTLE))
    ).checkWinner()
}
fun Game.handleTimeOutTurnZero(): Game {

    val player = players.find{ it.user.id == turn.playerTurn.first() }
        ?: throw GameError.PlayerNotFound(turn.playerTurn.first().toInt(), null, id.toInt())

    val nrTilesInHand = player.hand.numTileCards()

    return if(nrTilesInHand > 0) {
        handleTimeOutConstructionPhase(player).resolveTurnZero()
    } else {
        handleTimeOutSubstitutionPhase(player).resolveTurnZero()
    }
}
fun Game.handleTimeOutOutsideOfBattle(): Game {

    val player = players.find{ it.user.id == turn.playerTurn.first() }
        ?: throw GameError.PlayerNotFound(turn.playerTurn.first().toInt(), null, id.toInt())

    val nrTilesInHand = player.hand.numTileCards()
    val constructionPhase =
        if(turn.phase == TurnPhase.CONSTRUCTION && nrTilesInHand >= MAX_TILES_IN_HAND){
            handleTimeOutConstructionPhase(player)
        } else this

    val substitutionPhase =
        if(constructionPhase.turn.phase == TurnPhase.SUBSTITUTION && player.currentCharacter == null)
            handleTimeOutSubstitutionPhase(player)
        else constructionPhase

    return if(substitutionPhase.turn.playerTurn != turn.playerTurn) substitutionPhase
           else substitutionPhase.nextTurn()
}
private fun Game.handleTimeOutConstructionPhase(player: Player): Game {

    val randomTileCard = player.hand
        .filterValues { it.type == CardType.TILE }
        .entries
        .random()

    val tile = (randomTileCard.value as TileCard).tile
    val unoccupiedPositions = board
        .possibleUnoccupiedPositions()
        .shuffled()

    val placement = unoccupiedPositions.firstNotNullOfOrNull{ position ->
        tile.allRotations().firstOrNull{ rotation -> !board.checkBlocked(position, rotation) }
            ?.let{ rotation -> position to rotation }
    }

    val newGame =
        if(placement != null){
            val (pos, tile) = placement

            val updatedPlayers = players.map {
                if (it.user.id == player.user.id) player.removeFromHand(randomTileCard.key)
                else it
            }

            copy(
                board = board.placeTile(pos, tile),
                players = updatedPlayers
            )
        }
        else this

    return newGame.copy(turn = turn.copy(phase = TurnPhase.SUBSTITUTION))
}
private fun Game.handleTimeOutSubstitutionPhase(player: Player): Game {

    val anyCharacterInHand = player.hand.numCharacterCards() > 0
    val randomTile = board.tiles
        .filter{ it.character == null }
        .shuffled()
        .firstOrNull()

    return if(!anyCharacterInHand || randomTile == null) leaveGame(player, true)
    else{

        val randomCharacterCard = player.hand
            .filterValues{ it.type == CardType.CHARACTER }
            .entries
            .random()

        val character = (randomCharacterCard.value as CharacterCard).character

        val updatedPlayers = players.map {
            if (it.user.id == player.user.id) player.removeFromHand(randomCharacterCard.key)
            else it
        }
        val updatedBoard = board.placeCharacter(randomTile.pos, character)

        copy(
            board = updatedBoard,
            players = updatedPlayers,
        )
    }
}
fun Game.handleTimeOutStartingBattle(): Game {

    if(battle == null) throw GameError.NoBattleOngoing()

    val charactersReady = battle.pending.map{ it.origin.name }
    val charactersNotReady = battle.characters.filter{ character ->
        character.name !in charactersReady
    }

    val newGame = charactersNotReady.fold(this){ currentGame, character ->
        currentGame.leaveBattle(character)
    }

    return newGame.resolvePending()
}
fun Game.handleTimeOutDuringOrAfterBattle(): Game {

    if(battle == null) throw GameError.NoBattleOngoing()

    val charactersReady = battle.pending.map{ it.origin.name }
    val charactersNotReady = battle.characters.filter{ character ->
        character.name !in charactersReady
    }

    val newGame = charactersNotReady.fold(this){ currentGame, character ->
        currentGame.addActionToPending(character, null, PossibleBattleActions.FLEE)
    }

    return  if(battle.phase == BattlePhase.BATTLING) newGame.resolvePending()
            else newGame.deleteBattle()
}