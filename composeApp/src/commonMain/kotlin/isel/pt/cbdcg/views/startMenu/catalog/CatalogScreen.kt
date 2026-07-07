package isel.pt.cbdcg.views.startMenu.catalog

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.dp
import isel.pt.cbdcg.domain.game.Card
import isel.pt.cbdcg.domain.game.CharacterCard
import isel.pt.cbdcg.domain.game.ItemCard
import isel.pt.cbdcg.domain.game.TileCard
import isel.pt.cbdcg.domain.game.board.tile.AllTileEffects
import isel.pt.cbdcg.domain.game.character.ItemCatalog
import isel.pt.cbdcg.domain.game.character.PlayableCharacterCatalog
import isel.pt.cbdcg.views.game.utils.dialog.CardStatsDialog
import isel.pt.cbdcg.views.game.utils.dialog.TileEffectDialog
import kotlin.math.ceil
import kotlin.math.sqrt

enum class CatalogContent{ CHARACTERS, ITEMS, EVENTS, }

@Composable
fun CatalogScreen(
    mainMenuNav: () -> Unit,
    getDrawable: suspend (String) -> ImageBitmap,
) {

    var context by remember { mutableStateOf<CatalogContent?>(null) }
    var inspect by remember { mutableStateOf<Card?>(null) }

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

        Box{
            Column{
                Text(
                    text = "Game Catalog",
                    style = MaterialTheme.typography.headlineMedium,
                )
                Text(
                    text = "Click to check additional info.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        Spacer(Modifier.size(16.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ){

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                CatalogContent.entries.forEach { content ->
                    if(context == null || context == content){
                        CatalogOption(
                            option = content,
                            selected = context,
                            select = { context = if (context == content) null else content },
                            getDrawable = getDrawable
                        )
                    }
                }
            }

            Spacer(Modifier.size(16.dp))

            val currentContext = context
            val currentInspect = inspect

            if(currentContext != null){

                val catalogItems = when(currentContext){
                    CatalogContent.CHARACTERS ->
                        PlayableCharacterCatalog.basicCharacters +
                        PlayableCharacterCatalog.rareCharacters +
                        PlayableCharacterCatalog.epicCharacters
                    CatalogContent.ITEMS ->
                        ItemCatalog.commonItems +
                        ItemCatalog.specialItems
                    CatalogContent.EVENTS -> AllTileEffects.allTileEffects.keys.toList()
                }

                val nrOfRows = ceil(sqrt(catalogItems.size.toDouble())).toInt()

                Column(
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                ){
                    repeat(nrOfRows) { iteration ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
                        ) {
                            CatalogRow(
                                catalogItems = catalogItems,
                                context = currentContext,
                                nrOfRows = nrOfRows,
                                iteration = iteration,
                                getDrawable = getDrawable,
                                inspect = { inspect = it }
                            )
                        }
                    }
                }
            }
            if(currentInspect != null){
                when(currentInspect){
                    is CharacterCard,
                    is ItemCard ->
                        CardStatsDialog(
                            getDrawable = getDrawable,
                            card = currentInspect,
                            unequip = {  },
                            onDismiss = { inspect = null }
                        )
                    is TileCard -> {
                        TileEffectDialog(
                            getDrawable = getDrawable,
                            effect = currentInspect.tile.specialEffect,
                            activate = false,
                            onConfirm = { inspect = null  },
                        )
                    }
                }
            }
        }
    }
}