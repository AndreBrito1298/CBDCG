package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
fun StatRow(
    label: String,
    value: Int,
    color: Color,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(30.dp)
            .border(2.dp, color, RoundedCornerShape(6.dp))
            .background(Color.White, RoundedCornerShape(6.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(58.dp)
                .fillMaxSize()
                .background(color, RoundedCornerShape(topStart = 6.dp, bottomStart = 6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            statStars(value).forEach { tone ->
                Text(
                    text = "★",
                    color = tone.color(),
                    fontSize = 20.sp,
                    letterSpacing = 0.sp
                )
            }
        }
    }
}