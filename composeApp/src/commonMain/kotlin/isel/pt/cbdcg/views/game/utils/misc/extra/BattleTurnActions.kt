package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.BattleAction
import isel.pt.cbdcg.domain.game.PossibleBattleActions
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.ModifierType
import isel.pt.cbdcg.domain.game.character.StatModifier
import isel.pt.cbdcg.domain.game.character.Stats
import isel.pt.cbdcg.domain.game.character.adjustStats
import isel.pt.cbdcg.views.game.utils.ZoomedImage
import isel.pt.cbdcg.views.game.utils.misc.stats.StatsVariation

@Composable
fun BattleTurnActions(
    modifier: Modifier = Modifier,
    turn: Int,
    characters: List<Character>,
    actions: List<BattleAction>
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ){
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Turn $turn",
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Box(
                modifier = modifier
                    .border(2.dp, Color.Black)
                    .padding(8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ){
                    actions.fold(characters) { currentCharacters, battleAction ->

                        val origin = requireNotNull(currentCharacters.find { it.name == battleAction.origin.name })
                        val target = currentCharacters.find { it.name == battleAction.target?.name }

                        BattleAction(
                            modifier = Modifier.fillMaxSize(),
                            action = battleAction.action,
                            origin = origin.name,
                            target = target?.name,
                            stats = target?.adjustStats() ?: origin.adjustStats(),
                            deltaStats = battleAction.stats
                        )

                        when(battleAction.action){
                            PossibleBattleActions.ATTACK -> currentCharacters.map{ character ->
                                if(character.name == battleAction.target?.name)
                                    character.addModifier(
                                        StatModifier(
                                            stats = requireNotNull(battleAction.stats),
                                            duration = 0u,
                                            type = ModifierType.BATTLE_ATTACK
                                        )
                                    )
                                else character
                            }
                            PossibleBattleActions.HOLD -> currentCharacters.map{ character ->
                                if(character.name == battleAction.origin.name)
                                    character.addModifier(
                                        StatModifier(
                                            stats = requireNotNull(battleAction.stats),
                                            duration = 0u,
                                            type = ModifierType.BATTLE_HOLD
                                        )
                                    )
                                else character
                            }
                            PossibleBattleActions.FLEE -> currentCharacters.map{ character ->
                                if(character.name == battleAction.origin.name)
                                    character.addModifier(
                                        StatModifier(
                                            stats = requireNotNull(battleAction.stats),
                                            duration = 0u,
                                            type = ModifierType.BATTLE_FLEE
                                        )
                                    )
                                else character
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun BattleAction(
    modifier: Modifier = Modifier,
    action: PossibleBattleActions,
    origin: String,
    target: String? = null,
    stats: Stats,
    deltaStats: Stats
){

    val (color, action) =
        when(action){
            PossibleBattleActions.ATTACK -> Color.Red to "battle_attack"
            PossibleBattleActions.HOLD -> Color.Blue to "battle_hold"
            PossibleBattleActions.FLEE -> Color.Yellow to "battle_flee"
        }

    Box(
        modifier = modifier
            .width(120.dp)
            .border(1.dp, color)
            .padding(3.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ){

            ZoomedImage(
                modifier = Modifier.size(60.dp),
                fileName = origin,
                zoom = 1f,
            )

            ZoomedImage(
                modifier = Modifier.size(30.dp),
                fileName = action,
                zoom = 1f,
            )

            if(target != null){
                ZoomedImage(
                    modifier = Modifier.size(60.dp),
                    fileName = target,
                    zoom = 1f,
                )
            }

            StatsVariation(
                modifier = Modifier.fillMaxWidth(),
                stats = stats,
                deltaStats = deltaStats,
            )
        }
    }
}