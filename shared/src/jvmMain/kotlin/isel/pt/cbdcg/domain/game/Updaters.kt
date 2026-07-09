package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.BATTLE_TURN_DURATION_SECONDS
import isel.pt.cbdcg.domain.game.board.BoardTile
import isel.pt.cbdcg.domain.game.board.connectedNeighbours
import isel.pt.cbdcg.domain.game.board.connectionDistancesFrom
import isel.pt.cbdcg.domain.game.board.replaceBoardTile
import isel.pt.cbdcg.domain.game.board.tile.TileEffectTypes
import isel.pt.cbdcg.domain.game.board.tile.getStatModifier
import isel.pt.cbdcg.domain.game.board.tile.isPositive
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.GameError
import org.reflections.Reflections
import org.reflections.scanners.Scanners
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Grade
import isel.pt.cbdcg.domain.game.character.ItemEvolution
import isel.pt.cbdcg.domain.game.character.PlayableCharacter
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.special
import isel.pt.cbdcg.error.BattleError
import java.lang.reflect.Field
import java.lang.reflect.Modifier

@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.FIELD, AnnotationTarget.CLASS)
annotation class RegisterUpdater(val name: String)
private val funToNameMap: Map<String, UpdaterE<*, *>> by lazy {
    val reflections = Reflections("isel.pt.cbdcg.domain.game", Scanners.TypesAnnotated, Scanners.FieldsAnnotated)
    reflections.getFieldsAnnotatedWith(RegisterUpdater::class.java)
        .mapNotNull { field: Field ->
            if (!Modifier.isStatic(field.modifiers)) return@mapNotNull null
            field.isAccessible = true
            val instance = field.get(null) as? UpdaterE<*, *> ?: return@mapNotNull null
            val name = field.getAnnotation(RegisterUpdater::class.java).name
            name to instance
        }.toMap()
}

fun interface GameUpdater<T : Entity> {
    fun apply(game: Game, entity: Entity): Game
}
@Suppress("UNCHECKED_CAST")
private fun interface UpdaterE<T : Entity, R : Entity> {
    fun Game.apply(origin: T, targets: List<R>): Game
    fun Game.applyWithEntity(origin: Entity, targets: List<Entity>): Game {

        val list = targets.map { it as R }
        val result = this@UpdaterE.run { this@applyWithEntity.apply(origin as T, list) }
        return result
    }

    fun Game.addActionToPending(origin: Character, target: Character?, action: PossibleBattleActions): Game {
        if(this.battle == null)
            throw GameError.NoBattleOngoing()

        if(battle.pending.any{ it.origin.name == origin.name })
            throw BattleError.ActionAlreadyQueued()

        val battleAction =
            BattleAction(
                origin = origin,
                target = target,
                action = action,
                stats = Stats(),
                turn = turn.gameTurn.toInt()
            )
        return copy(battle = battle.copy(pending = battle.pending + battleAction))
    }
}

fun Game.gameUpdateByName(name: String, origin: Entity, targets: List<Entity>): Game {
    val updater = funToNameMap[name] ?: throw IllegalArgumentException("Updater not found")
    println("RUNNING UPDATER: $name")
    try {
        return updater.run { this@gameUpdateByName.applyWithEntity(origin, targets) }
    }
    catch (e: Exception) {
        println(e)
        throw e
    }
}

@field:RegisterUpdater("CharacterMovement")
private val CharacterMovement = UpdaterE<BoardTile, BoardTile> { origin, targets ->
    if (this.turn.phase != TurnPhase.MOVEMENT) throw GameError.CharacterMovementRestriction()
    val newStartingTile = origin.copy(character = null)
    val endTile = targets.firstOrNull() ?: throw BoardError.NoTargetFound()
    if (endTile.character != null) throw BoardError.TileOccupied()
    val newEndingTile = targets.first().copy(character = origin.character)
    copy(
        board = board
            .replaceBoardTile(newStartingTile)
            .replaceBoardTile(newEndingTile)
    )
}

@field:RegisterUpdater("DrawItem")
private val DrawItem = UpdaterE<Player, BoardTile> { player, targets ->
    val boardTile = targets.firstOrNull() ?: throw BoardError.NoTargetFound()

    if (player.user.id != turn.playerTurn.first())
        throw GameError.NotYourTurn()

    if (boardTile.cooldown != 0u)
        throw BoardError.EffectInCooldown(boardTile.tile.specialEffect.type.name, boardTile.cooldown!!.toInt())

    val newBoard = board.tiles.map {
        if (it == boardTile) boardTile.copy(cooldown = boardTile.tile.specialEffect.maxCooldown)
        else it
    }

    val items =
        when (boardTile.tile.specialEffect.type) {

            TileEffectTypes.Chest -> listOf(itemDeck.filter { (item, _) -> !item.grade.special() }.draw())

            TileEffectTypes.BigChest -> {
            val commonItem = itemDeck
                .filter { (item, _) -> !item.grade.special() }
                .draw()

            val charactersOnBoard = board.tiles.mapNotNull { it.character }
            val evolveItems = charactersOnBoard
                .filter { it.evolution != null && it.evolution is ItemEvolution }
                .map { (it.evolution as ItemEvolution).item }

            val specialItem = itemDeck
                .filter { (item, _) -> item.grade.special() && (item.grade == Grade.KEY || item.name in evolveItems) }
                .draw()

            listOf(commonItem, specialItem)
        }
        else -> throw GameError.InvalidFormat("Draw Item", boardTile.tile.specialEffect.type.name)
    }

    val updatedPlayers = players.map {
        if (it.user.id == player.user.id) items.fold(player) { p, item -> p.addToHand(ItemCard(item)) }
        else it
    }

    val updatedItemDeck = items.fold(itemDeck) { deck, item -> deck.remove(item) }

    this.copy(
        players = updatedPlayers,
        board = board.copy(tiles = newBoard),
        itemDeck = updatedItemDeck,
    )
}

@field:RegisterUpdater("UpdateStatModifiers")
private val UpdateStatModifiers = UpdaterE<Player, BoardTile> { player, targets ->
    val boardTile = targets.firstOrNull() ?: throw BoardError.NoTargetFound()

    if (player.user.id != turn.playerTurn.first())
        throw GameError.NotYourTurn()

    if (boardTile.cooldown != 0u)
        throw BoardError.EffectInCooldown(boardTile.tile.specialEffect.type.name, boardTile.cooldown!!.toInt())

    val character = (boardTile.character as? PlayableCharacter)
        ?: throw BoardError.EmptyTile()

    if (character.name != player.currentCharacter)
        throw BoardError.ApplyEffectOnYourCharacter()

    val specialEffect = boardTile.tile.specialEffect
    val statModifier = specialEffect.getStatModifier()
    val range = specialEffect.range.toInt()

    val distances = board.connectionDistancesFrom(boardTile)
    val affectedTiles =
        if(specialEffect.isPositive()) distances.filter { it.value <= range }.keys
        else distances.filter{ it.value in 1..range }.keys

    val newBoard = board.tiles.map { tile ->
        val isOrigin = tile.pos == boardTile.pos
        val affectedTile = affectedTiles.find { it.pos == tile.pos }
        val newCooldown = if (isOrigin) specialEffect.maxCooldown else tile.cooldown

        val updatedCharacter = if (affectedTile != null && tile.character is PlayableCharacter) {
            tile.character.copy(activeStatModifiers = tile.character.activeStatModifiers + statModifier)
        } else tile.character

        tile.copy(cooldown = newCooldown, character = updatedCharacter)
    }

    this.copy(board = board.copy(tiles = newBoard))
}

@field:RegisterUpdater("BattleStart")
private val BattleStart = UpdaterE<Character, Character> { attacker, targets ->
    val defender = targets.firstOrNull() ?: throw BoardError.NoTargetFound()

    if (this.battle != null) throw GameError.BattleNotConcluded()
    if (this.turn.phase != TurnPhase.MOVEMENT) throw GameError.MoveToBattleRestriction()

    val mainCharacters = listOf(attacker, defender)
    val mainPlayers = players.filter {
        it.currentCharacter in mainCharacters.map { character -> character.name }
    }
    val mainCharactersReady = mainCharacters.map { character ->
        BattleAction(origin = character, target = null, action = PossibleBattleActions.FLEE, stats = Stats(), turn = turn.gameTurn.toInt())
    }
    val itemBet = mainPlayers.mapNotNull { player ->
        val itemCards = player.hand.mapNotNull { card -> (card.value as? ItemCard) }
        val keyItems = itemCards.filter { itemCard -> itemCard.item.grade == Grade.KEY }

        if (keyItems.isNotEmpty()) BattleBet(player, keyItems.random().item)
        else if (itemCards.isNotEmpty()) BattleBet(player, itemCards.random().item)
        else null
}

    val participatingCharacterTiles = board.tiles.filter { boardTile ->
        val characterInTile = boardTile.character
        characterInTile != null && mainCharacters.any { it.name == characterInTile.name }
    }

    val adjacentCharacters = participatingCharacterTiles
        .flatMap { boardTile -> board.connectedNeighbours(boardTile) }
        .mapNotNull { neighbour -> neighbour.character }
        .filter { character -> character.name !in mainCharacters.map { it.name } }
        .distinct()

    val charactersInBattle = mainCharacters + adjacentCharacters

    this.copy(
        battle = Battle(
            characters = charactersInBattle,
            itemBet = itemBet,
            pending = mainCharactersReady,
            phase = BattlePhase.WAITING
        ),
        turn = turn.copy(deadline = newDeadline(BATTLE_TURN_DURATION_SECONDS))
    ).resolvePending()
}

@field:RegisterUpdater("JoinBattle")
private val JoinBattle = UpdaterE<Character, Character> { character, none ->
    val currentBattle = this.battle ?: throw GameError.NoBattleOngoing()
    val player = players.find { it.currentCharacter == character.name }
        ?: throw BattleError.CharacterNotFound(character.name)

    val ready = BattleAction(origin = character, target = null,
        action = PossibleBattleActions.PASSIVE, stats = Stats(), turn = turn.gameTurn.toInt())

    val itemCards = player.hand.mapNotNull { card -> (card.value as? ItemCard) }
    val keyItems = itemCards.filter { itemCard -> itemCard.item.grade == Grade.KEY }

    val bet = if (keyItems.isNotEmpty()) BattleBet(player, keyItems.random().item)
    else if (itemCards.isNotEmpty()) BattleBet(player, itemCards.random().item)
    else BattleBet(player, null)

    copy(
        battle = currentBattle.copy(
            pending = currentBattle.pending + ready,
            itemBet = currentBattle.itemBet + bet
        )
    ).resolvePending()
}

@field:RegisterUpdater("AddActionToPending")
private val AddActionToPending = UpdaterE<BattleAction, BattleAction> { action, none->
    if(battle == null)
        throw GameError.NoBattleOngoing()

    if(battle.pending.any{ it.origin.name == action.origin.name })
        throw BattleError.ActionAlreadyQueued()

    copy(battle = battle.copy(pending = battle.pending + action)).resolvePending()
}

@field:RegisterUpdater("LeaveBattle")
private val LeaveBattle= UpdaterE<Character, Character> { character, none->
    val currentBattle = battle ?: throw GameError.NoBattleOngoing()

    val idx = currentBattle.characters.indexOfFirst { it.name == character.name }
    if(idx == 0 || idx == 1) throw BattleError.CantLeaveBattle()

    this.copy(battle = currentBattle.copy(
        characters = currentBattle.characters.filterNot { it.name == character.name },
        pending = currentBattle.pending.filterNot { it.origin.name == character.name },
        itemBet = currentBattle.itemBet.filterNot { it.player.currentCharacter == character.name }
    )).resolvePending()
}

@field:RegisterUpdater("EndBattle")
private val EndBattle= UpdaterE<BattleAction, Character> { action, none->
    if(battle == null)
        throw GameError.NoBattleOngoing()

    if(battle.pending.any{ it.origin.name == action.origin.name })
        throw BattleError.ActionAlreadyQueued()

    copy(battle = battle.copy(pending = battle.pending + action)).deleteBattle()
}

@field:RegisterUpdater("RemoveActionFromPending")
private val RemoveActionFromPending= UpdaterE<Character, BattleAction> { character, none->
    if(battle == null)
        throw GameError.NoBattleOngoing()

    val action = battle.pending.find{ it.origin == character }
        ?: throw BattleError.ActionNotQueued()

    this.copy(battle = battle.copy(pending = battle.pending - action)).resolvePending()
}

fun Game.handleTimeOutStartingBattle(): Game {

    if(battle == null) throw GameError.NoBattleOngoing()

    val charactersReady = battle.pending.map{ it.origin.name }
    val charactersNotReady = battle.characters.filter{ character ->
        character.name !in charactersReady
    }

    if (charactersNotReady.isEmpty()) return resolvePending()

    val readyNames = charactersReady.toSet()
    val newBattle = battle.copy(
        characters = battle.characters.filter { it.name in readyNames },
        pending = battle.pending.filter { it.origin.name in readyNames },
        itemBet = battle.itemBet.filter { it.player.currentCharacter in readyNames }
    )

    return copy(battle = newBattle).resolvePending()
}
fun Game.handleTimeOutDuringOrAfterBattle(): Game {
    if(battle == null) throw GameError.NoBattleOngoing()

    val charactersReady = battle.pending.map{ it.origin.name }
    val charactersNotReady = battle.characters.filter{ character ->
        character.name !in charactersReady
    }

    val newGame = charactersNotReady.fold(this){ currentGame, character ->
        currentGame.gameUpdateByName("AddActionToPending", BattleAction(character, null, PossibleBattleActions.FLEE, Stats(), turn = turn.gameTurn.toInt()), emptyList())
    }

    return  if(battle.phase == BattlePhase.BATTLING) newGame.resolvePending()
    else newGame.deleteBattle()
}