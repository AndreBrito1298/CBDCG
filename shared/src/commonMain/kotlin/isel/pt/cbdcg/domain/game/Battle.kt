package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.toCharacter
import isel.pt.cbdcg.dto.BattleDTO

data class Battle(
    val characters: List<Character>,
    val turn: UInt,
    val currentTurn: Character
)

fun Battle.toBattleDTO(): BattleDTO =
    BattleDTO(
        characters = characters.map{ it.toCharacterDTO() }.toTypedArray(),
        turn = turn.toInt(),
        currentTurn = currentTurn.toCharacterDTO()
    )
fun BattleDTO.toBattle(): Battle =
    Battle(
        characters = characters.map{ it.toCharacter() },
        turn = turn.toUInt(),
        currentTurn = currentTurn.toCharacter()
    )

