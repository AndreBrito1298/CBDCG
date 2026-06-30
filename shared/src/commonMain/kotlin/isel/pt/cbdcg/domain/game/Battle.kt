package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.BASE_HOLD_DEFENCE_BOOST
import isel.pt.cbdcg.HOLD_BONUS_FLEE_CHANCE
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.Item
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.domain.game.character.toItem
import isel.pt.cbdcg.domain.game.character.toItemDTO
import isel.pt.cbdcg.domain.game.character.toStats
import isel.pt.cbdcg.dto.BattleActionDTO
import isel.pt.cbdcg.dto.BattleBetDTO
import isel.pt.cbdcg.dto.BattleDTO
import isel.pt.cbdcg.error.BattleError
import kotlin.math.min
import kotlin.random.Random

enum class BattlePhase{
    WAITING, BATTLING, ENDING
}
fun String.toBattlePhase(): BattlePhase =
    when(this){
        "WAITING" -> BattlePhase.WAITING
        "BATTLING" -> BattlePhase.BATTLING
        "ENDING" -> BattlePhase.ENDING
        else -> throw BattleError.InvalidAction(this)
    }

enum class PossibleBattleActions {
    HOLD, FLEE, ATTACK
}

fun String.toPossibleBattleAction(): PossibleBattleActions =
    when(this){
        "HOLD" -> PossibleBattleActions.HOLD
        "FLEE" -> PossibleBattleActions.FLEE
        "ATTACK" -> PossibleBattleActions.ATTACK
        else -> throw BattleError.InvalidAction(this)
    }

data class BattleAction(
    val origin: Character,
    val target: Character?,
    val action: PossibleBattleActions,
    val stats: Stats
)

fun BattleAction.toBattleActionDTO(turn: UInt): BattleActionDTO =
    BattleActionDTO(
        turn = turn.toInt(),
        origin = origin.toCharacterDTO(),
        target = target?.toCharacterDTO(),
        action = action.name,
        stats = stats.toString()
    )

fun BattleActionDTO.toBattleAction(): BattleAction =
    BattleAction(
        origin = origin.toCharacter(),
        target = target?.toCharacter(),
        action = action.toPossibleBattleAction(),
        stats = stats.toStats()
    )

data class BattleBet(
    val player: Player,
    val item: Item?
)

fun BattleBet.toBattleBetDTO(): BattleBetDTO =
    BattleBetDTO(
        player = player.toPlayerDTO(),
        item = item?.toItemDTO()
    )

fun BattleBetDTO.toBattleBet(): BattleBet =
    BattleBet(
        player = player.toPlayer(),
        item = item?.toItem()
    )

data class Battle(
    val characters: List<Character>,
    val currentTurn: UInt = 0u,
    val phase: BattlePhase,
    val pending: List<BattleAction> = emptyList(),
    val actions: Map<UInt, List<BattleAction>> = emptyMap(),
    val itemBet: List<BattleBet>
)


fun Battle.toBattleDTO(): BattleDTO =
    BattleDTO(
        characters = characters.map{ it.toCharacterDTO() }.toTypedArray(),
        currentTurn = currentTurn.toInt(),
        phase = phase.name,
        pending = pending.map{ it.toBattleActionDTO(currentTurn) }.toTypedArray(),
        actions = actions.flatMap{ (key, value) -> value.map{ it.toBattleActionDTO(key)} }.toTypedArray(),
        itemBet = itemBet.map{ it.toBattleBetDTO() }.toTypedArray()
    )
fun BattleDTO.toBattle(): Battle =
    Battle(
        characters = characters.map { it.toCharacter() },
        currentTurn = currentTurn.toUInt(),
        phase = phase.toBattlePhase(),
        pending = pending.map { it.toBattleAction() },
        actions = actions
            .groupBy{ it.turn.toUInt() }
            .mapValues{ (_, dto) -> dto.map{ it.toBattleAction() } },
        itemBet = itemBet.map { it.toBattleBet() }
    )

fun Battle.attack(battleAction: BattleAction): Battle {

    val origin = characters.find { it.name == battleAction.origin.name }
        ?: throw BattleError.CharacterNotFound(battleAction.origin.name)
    val originBattleStats = origin.adjustStats()
    val target = characters.find { it.name == battleAction.target?.name }
        ?: throw BattleError.CharacterNotFound(battleAction.target?.name ?: "")
    val targetBattleStats = target.adjustStats()

    val dodge = targetBattleStats.spe - originBattleStats.spe
    val dodgeChance = (dodge + 1) / 25f
    val damage = originBattleStats.dmg - targetBattleStats.def

    val stats =
        if(Random.nextFloat() > dodgeChance)
            Stats(
                hp = if(damage > 0) -min(targetBattleStats.hp, damage) else 0,
                dmg = 0,
                def = if(targetBattleStats.def <= 0) 0
                      else if(damage > 0) -target.adjustStats().def
                      else -originBattleStats.dmg,
                spe = 0
            )
        else Stats()

    val updatedCharacters = characters.map { character ->
        if (character.name == target.name)
            character.addModifier(
                StatModifier(
                    stats = stats,
                    duration = 0u,
                    type = ModifierType.BATTLE_ATTACK
                )
            )
        else character
    }

    return copy(
        characters = updatedCharacters,
        actions = actions + (currentTurn to ((actions[currentTurn] ?: emptyList()) + battleAction.copy(stats = stats)))
    )
}
fun Battle.hold(battleAction: BattleAction): Battle {

    val origin = characters.find { it.name == battleAction.origin.name }
        ?: throw BattleError.CharacterNotFound(battleAction.origin.name)

    val heldLastTurn = actions.any { (turn, action) ->
        turn == currentTurn - 1u && action.any{ it.origin.name == origin.name && it.action == PossibleBattleActions.HOLD }
    }
    if(heldLastTurn) return this

    val currentDef = origin.adjustStats().def
    val restoreBaseDef = origin.baseStats.def - currentDef
    val newDef = if(restoreBaseDef > 0) restoreBaseDef else BASE_HOLD_DEFENCE_BOOST

    val stats = Stats(hp = 0, dmg = 0, def = newDef, spe = 0)

    val updatedCharacters = characters.map{ character ->
        if(character.name == origin.name){
            character.addModifier(
                StatModifier(
                    stats = stats,
                    duration = 0u,
                    type = ModifierType.BATTLE_HOLD
                )
            )
        }
        else character
    }

    return copy(
        characters = updatedCharacters,
        actions = actions + (currentTurn to ((actions[currentTurn] ?: emptyList()) + battleAction.copy(stats = stats)))
    )
}
fun Battle.flee(battleAction: BattleAction): Battle {

    val origin = characters.find { it.name == battleAction.origin.name }
        ?: throw BattleError.CharacterNotFound(battleAction.origin.name)

    val heldLastTurn = actions.any { (turn, action) ->
        turn == currentTurn - 1u && action.any{ it.origin.name == origin.name && it.action == PossibleBattleActions.HOLD }
    }
    val lostHPRecently = origin.activeStatModifiers
        .filter{ it.type == ModifierType.BATTLE_ATTACK }
        .sortedBy{ it.duration }
        .firstOrNull()
        .let{ it != null && it.stats.hp < 0 }

    val speedDiff = characters
        .map { origin.adjustStats().spe - it.adjustStats().spe }
        .maxOf { it }
        .coerceAtLeast(0)

    val baseFleeChance = speedDiff / 10f
    val increasedChance = if(heldLastTurn && !lostHPRecently) HOLD_BONUS_FLEE_CHANCE else 0.0
    val failedToFlee = Random.nextFloat() > baseFleeChance + increasedChance

    val stats =
        if(failedToFlee) Stats()
        else Stats(-99,0,0,0)

    val updatedCharacters = characters.map { character ->
        if (character.name == origin.name)
            character.addModifier(
                StatModifier(
                    stats = stats,
                    duration = 0u,
                    type = ModifierType.BATTLE_FLEE
                )
            )
        else character
    }

    return copy(
        characters = updatedCharacters,
        actions = actions + (currentTurn to ((actions[currentTurn] ?: emptyList()) + battleAction.copy(stats = stats))),
        itemBet = if(failedToFlee) itemBet
                  else itemBet.filter{ it.player.currentCharacter != origin.name }

    )
}
fun Battle.incrementModifiers(): Battle =
    copy(
        characters = characters.map{ it.increaseInBattleModifierTurn() }
    )