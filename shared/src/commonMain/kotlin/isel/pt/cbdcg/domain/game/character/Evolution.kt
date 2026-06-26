package isel.pt.cbdcg.domain.game.character

import isel.pt.cbdcg.dto.EvolutionDTO
import isel.pt.cbdcg.error.GameError

enum class EvolutionType {
    IN_BATTLE, ITEM, NR_BATTLES
}
fun String.toEvolutionType(): EvolutionType =
    when(this){
        "IN_BATTLE" -> EvolutionType.IN_BATTLE
        "ITEM" -> EvolutionType.ITEM
        "NR_BATTLES" -> EvolutionType.NR_BATTLES
        else -> throw GameError.InvalidFormat("Evolution Type", this)
    }

enum class InBattleEvolutionConditions{
    BLOCK, DAMAGE, REGEN, PERISH
}
fun String.toInBattleEvolutionConditions(): InBattleEvolutionConditions =
    when(this){
        "BLOCK" -> InBattleEvolutionConditions.BLOCK
        "DAMAGE" -> InBattleEvolutionConditions.DAMAGE
        "REGEN" -> InBattleEvolutionConditions.REGEN
        "PERISH" -> InBattleEvolutionConditions.PERISH
        else -> throw GameError.InvalidFormat("Evolution Condition", this)
    }

enum class MultipleBattlesEvolutionConditions{
    WIN, LOSE, BATTLE, FLEE
}
fun String.toMultipleBattlesEvolutionConditions(): MultipleBattlesEvolutionConditions =
    when(this){
        "WIN" -> MultipleBattlesEvolutionConditions.WIN
        "LOSE" -> MultipleBattlesEvolutionConditions.LOSE
        "BATTLE" -> MultipleBattlesEvolutionConditions.BATTLE
        "FLEE" -> MultipleBattlesEvolutionConditions.FLEE
        else -> throw GameError.InvalidFormat("Evolution Condition", this)
    }

data class BattleEvolution(
    override val type: EvolutionType = EvolutionType.IN_BATTLE,
    override val character: String,
    val condition: InBattleEvolutionConditions,
    val value: Int,
) : Evolution {
    override fun toEvolutionDTO(): EvolutionDTO =
        EvolutionDTO(
            type = type.name,
            character = character,
            condition = condition.name,
            value = value
        )
}
fun EvolutionDTO.toBattleEvolution(): BattleEvolution =
    BattleEvolution(
        type = type.toEvolutionType(),
        character = character,
        condition = condition?.toInBattleEvolutionConditions() ?: throw GameError.InvalidFormat("Evolution Condition", condition.toString()),
        value = value ?: throw GameError.InvalidFormat("Evolution Value", value.toString())
    )
fun BattleEvolution.description(): String =
    when (condition) {
        InBattleEvolutionConditions.BLOCK ->
            "In a single battle, block a total of $value damage"
        InBattleEvolutionConditions.DAMAGE ->
            "In a single battle, deal a total of $value unblocked damage"
        InBattleEvolutionConditions.REGEN ->
            "In a single battle, regenerate a total of $value HP"
        InBattleEvolutionConditions.PERISH ->
            "In a single battle, survive with $value HP"
    }

data class ItemEvolution(
    val item: String,
    override val type: EvolutionType = EvolutionType.ITEM,
    override val character: String
) : Evolution {
    override fun toEvolutionDTO(): EvolutionDTO =
        EvolutionDTO(
            type = type.name,
            character = character,
            item = item
        )
}
fun EvolutionDTO.toItemEvolution(): ItemEvolution =
    ItemEvolution(
        type = EvolutionType.ITEM,
        character = character,
        item = item ?: throw GameError.InvalidFormat("Evolution Item", item.toString())
    )

data class MultipleBattlesEvolution(
    override val type: EvolutionType = EvolutionType.NR_BATTLES,
    override val character: String,
    val value: Int,
    val currentValue: Int = 0,
    val condition: MultipleBattlesEvolutionConditions
) : Evolution{
    override fun toEvolutionDTO(): EvolutionDTO =
        EvolutionDTO(
            type = type.name,
            character = character,
            value = value,
            currentValue = currentValue,
            condition = condition.name
        )
}
fun EvolutionDTO.toMultipleBattlesEvolution(): MultipleBattlesEvolution =
    MultipleBattlesEvolution(
        type = EvolutionType.NR_BATTLES,
        character = character,
        value = value ?: throw GameError.InvalidFormat("Evolution Value", value.toString()),
        currentValue = currentValue ?: throw GameError.InvalidFormat("Evolution Current Value", currentValue.toString()),
        condition = condition?.toMultipleBattlesEvolutionConditions() ?: throw GameError.InvalidFormat("Evolution Condition", condition.toString())
    )
fun MultipleBattlesEvolution.description(): String =
    when (condition) {
        MultipleBattlesEvolutionConditions.WIN ->
            "Win a total of $value battles"
        MultipleBattlesEvolutionConditions.LOSE ->
            "Lose a total of $value battles"
        MultipleBattlesEvolutionConditions.BATTLE ->
            "Survive a total of $value battles"
        MultipleBattlesEvolutionConditions.FLEE ->
            "Successfully flee from $value battles"
    }

sealed interface Evolution{
    val type: EvolutionType
    val character: String

    fun toEvolutionDTO(): EvolutionDTO
}

fun EvolutionDTO.toEvolution(): Evolution =
    when(type.toEvolutionType()){
        EvolutionType.IN_BATTLE -> toBattleEvolution()
        EvolutionType.ITEM -> toItemEvolution()
        EvolutionType.NR_BATTLES -> toMultipleBattlesEvolution()
    }