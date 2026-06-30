package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SimpleClock(
    modifier: Modifier = Modifier,
    remainingSeconds: Long,
){
    Box(
        modifier = modifier
            .background(Color.White, CircleShape)
            .border(1.dp, Color.Black, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = remainingSeconds.toString(),
            fontSize = 18.sp,
            color = Color.Black
        )
    }
}