package isel.pt.cbdcg.views.startMenu.tutorial

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun TutorialOptionsRow(
    nrOfRows: Int,
    iteration: Int,
    options: Map<Int, TutorialOptions>,
    getDrawable: suspend (String) -> ImageBitmap,
    selected: TutorialOptions?,
    select: (TutorialOptions) -> Unit
){

    for (k in 0 until nrOfRows) {

        val optionIndex = iteration * nrOfRows + k
        val option = options.get(optionIndex) ?: continue
        val borderColor = if(selected == option) Color.Green else Color.Black

        Column(
            modifier = Modifier.size(148.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ZoomedImage(
                fileName = option.imageName,
                zoom = 1f,
                loadDrawable = { getDrawable(option.imageName) },
                modifier = Modifier
                    .size(68.dp)
                    .border(1.dp, borderColor)
                    .clickable{ select(option) }
                    .padding(4.dp)
            )

            Text(text = option.name.replace("_", " "), fontSize = 15.sp)
        }
    }

}