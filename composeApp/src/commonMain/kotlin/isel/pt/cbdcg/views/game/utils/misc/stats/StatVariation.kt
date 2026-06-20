package isel.pt.cbdcg.views.game.utils.misc.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatVariation(
    modifier: Modifier = Modifier,
    label: String,
    value: Int,
    delta: Int,
    color: Color,
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .border(2.dp, color, RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .background(color, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
            contentAlignment = Alignment.Center
        ) {

            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 6.sp,
            )
        }

        Row(
            modifier = Modifier
                .weight(4f)
                .padding(horizontal = 5.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
        ) {
            statVariation(value, delta).forEach { tone ->
                Text(
                    text = "★",
                    color = tone.color(),
                    fontSize = 12.sp,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}