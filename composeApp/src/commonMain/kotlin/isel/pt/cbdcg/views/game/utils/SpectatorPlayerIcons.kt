package isel.pt.cbdcg.views.game.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cbdcg.composeapp.generated.resources.Res
import cbdcg.composeapp.generated.resources.allDrawableResources
import org.jetbrains.compose.resources.painterResource

@Composable
fun SpectatorPlayerIcons(
    characterName: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
){
    Box(
        modifier = Modifier
            .size(128.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = Color.Black
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {

        val resourceName = characterName ?: "missing_texture"

        val resource = Res.allDrawableResources[resourceName]
            ?: error("Drawable not found: $characterName")

        Image(
            painter = painterResource(resource),
            contentDescription = "Personagem: $characterName",
            modifier = Modifier.size(128.dp)
        )

    }
}

