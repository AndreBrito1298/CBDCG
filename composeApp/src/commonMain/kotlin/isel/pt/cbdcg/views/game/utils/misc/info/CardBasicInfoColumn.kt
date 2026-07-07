package isel.pt.cbdcg.views.game.utils.misc.info

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun CardBasicInfoColumn(
    modifier: Modifier,
    mainText: String,
    zoom: Float,
    subText: String,
    getDrawable: suspend (String) -> ImageBitmap,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .border(1.dp, Color.Black)
                    .padding(8.dp)
            ) {
                ZoomedImage(
                    fileName = mainText,
                    loadDrawable = { getDrawable(mainText) },
                    modifier = Modifier.size(128.dp),
                    zoom = zoom
                )
            }

            Text(
                text = mainText.replace('_', ' ').uppercase(),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = subText,
                fontSize = 13.sp
            )
        }
    }
}