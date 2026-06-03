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
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.views.game.utils.ZoomedImage

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
            .padding(8.dp)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {

        val (resourceName, zoom) =
            if(characterName == null) "missing_texture" to 1.0f
            else characterName to 2.0f

        ZoomedImage(
            fileName = resourceName,
            zoom = zoom,
            modifier = Modifier.size(64.dp)
        )

    }
}

