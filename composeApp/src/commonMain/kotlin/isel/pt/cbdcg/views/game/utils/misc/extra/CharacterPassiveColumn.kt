package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.websocket.Frame
import isel.pt.cbdcg.domain.game.character.Character


@Composable
fun CharacterPassiveColumn(character: Character, modifier: Modifier) {
    Text(
        text = character.passiveProps.description,
        fontWeight = FontWeight.Thin,
        fontSize = 13.sp,
        lineHeight = 15.sp,
        modifier = modifier.padding(bottom = 8.dp),
        overflow = TextOverflow.Ellipsis,
        maxLines = 6,
    )
}