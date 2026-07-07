package isel.pt.cbdcg.views.startMenu.tutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import kotlin.math.ceil
import kotlin.math.sqrt

@Composable
fun ChooseTutorial(
    mainMenuNav: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
    selectedTutorial: TutorialOptions?,
    selectTutorial: (TutorialOptions) -> Unit,
){

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ){
        Button(onClick = mainMenuNav) {
            Text("Back")
        }

        Box {
            Column {
                Text(
                    text = "Tutorial Selection",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Click twice to enter a tutorial.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.size(16.dp))

        val nrOfRows = ceil(sqrt(TutorialOptions.entries.size.toDouble())).toInt()
        val options = TutorialOptions.entries.associateBy { it.ordinal }

        repeat(nrOfRows) { iteration ->

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            ) {
                TutorialOptionsRow(
                    nrOfRows = nrOfRows,
                    iteration = iteration,
                    options = options,
                    getDrawable = getDrawable,
                    selected = selectedTutorial,
                    select = { selectTutorial(it) }
                )
            }
        }
    }

}