package isel.pt.cbdcg.views.game.utils.misc.info

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.isBattleMod
import isel.pt.cbdcg.views.game.utils.misc.extra.BattleTurnActions


@Composable
fun ActionsInfo(
    modifier: Modifier = Modifier,
    characters: List<Character>,
    currentTurn: UInt,
    actions: Map<UInt, List<BattleAction>>,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        actions.forEach{ (previewTurn, actions) ->

            val charactersThisTurn = characters.map { character ->
                character.activeStatModifiers
                    .filter{ mod ->
                        val turnOfAction = currentTurn - mod.duration - 1u
                        mod.type.isBattleMod() && turnOfAction >= previewTurn
                    }
                    .fold(character) { currentCharacter, mod ->
                        currentCharacter.removeModifier(mod)
                    }
            }

            BattleTurnActions(
                modifier = Modifier.fillMaxSize(),
                turn = previewTurn.toInt(),
                characters = charactersThisTurn,
                actions = actions,
            )
        }
    }
}