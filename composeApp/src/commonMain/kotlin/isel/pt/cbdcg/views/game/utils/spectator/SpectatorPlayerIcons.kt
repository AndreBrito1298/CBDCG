package isel.pt.cbdcg.views.game.utils.spectator

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.views.game.utils.misc.extra.ZoomedImage

@Composable
fun SpectatorPlayerIcons(
    characterName: String?,
    isSelected: Boolean,
    onClick: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
){
    Box(
        modifier = Modifier
            .size(128.dp)
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = Color.Black
            )
            .padding(8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        val resourceName = characterName ?: "missing_texture"

        ZoomedImage(
            fileName = resourceName,
            loadDrawable = { getDrawable(resourceName) },
            zoom = 1.0f,
            modifier = Modifier.size(64.dp)
        )

    }
}

