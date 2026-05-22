package isel.pt.cbdcg.views.game.utils.board

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults.outlinedButtonColors
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ZoomButtons(
    modifier: Modifier,
    amplify: () -> Unit,
    reduce: () -> Unit,
){
    Row(
        modifier = modifier
    ) {
        OutlinedButton(
            onClick = reduce,
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(0.dp),
            colors = outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text("-")
        }

        Spacer(modifier = Modifier.width(4.dp))

        OutlinedButton(
            onClick = amplify,
            modifier = Modifier.size(32.dp),
            contentPadding = PaddingValues(0.dp),
            colors = outlinedButtonColors(
                containerColor = Color.White,
                contentColor = Color.Black
            ),
            border = BorderStroke(1.dp, Color.Black)
        ) {
            Text("+")
        }
    }
}