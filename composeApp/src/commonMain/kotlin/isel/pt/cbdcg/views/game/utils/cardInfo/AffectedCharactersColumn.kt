package isel.pt.cbdcg.views.game.utils.cardInfo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.views.game.utils.ZoomedImage

@Composable
fun AffectedCharactersColumn(
    modifier: Modifier,
    characters: List<Character>,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Affected Characters",
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (characters.isEmpty()) {
            Text(
                text = "No characters affected",
                fontSize = 12.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(characters) { character ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ZoomedImage(
                            fileName = character.name,
                            zoom = 1.0f,
                            modifier = Modifier.width(64.dp).height(64.dp)
                        )

                        Text(
                            text = character.name,
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp,
                            modifier = Modifier.width(70.dp)
                        )
                    }
                }
            }
        }
    }
}