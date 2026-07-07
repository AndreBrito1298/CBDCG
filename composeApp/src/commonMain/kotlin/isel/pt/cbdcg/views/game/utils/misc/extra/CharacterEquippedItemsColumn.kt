package isel.pt.cbdcg.views.game.utils.misc.extra

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
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
import isel.pt.cbdcg.domain.game.character.Character
import isel.pt.cbdcg.domain.game.character.PlayableCharacter

@Composable
fun CharacterEquippedItemsColumn(
    modifier: Modifier,
    unequip: (Int) -> Unit,
    character: Character,
    getDrawable: suspend (String) -> ImageBitmap,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (character is PlayableCharacter) {
            Text(
                text = "Equipped Items",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                itemsIndexed(character.items) { index, item ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ZoomedImage(
                            fileName = item.name,
                            loadDrawable = { getDrawable(item.name) },
                            modifier = Modifier.size(128.dp),
                            zoom = 1.0f,
                        )

                        Text(
                            text = item.name.replace('_', ' '),
                            textAlign = TextAlign.Center,
                            fontSize = 10.sp
                        )

                        Button(
                            onClick = { unequip(index) },
                            modifier = Modifier.padding(top = 4.dp).height(24.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                        ) {
                            Text(
                                text = "Unequip",
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
            
            if (character.items.isEmpty()) {
                Text(
                    text = "No items equipped",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
