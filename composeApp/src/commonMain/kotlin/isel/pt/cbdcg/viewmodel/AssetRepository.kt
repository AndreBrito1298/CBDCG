package isel.pt.cbdcg.viewmodel

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.decodeToImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class AssetRepository(
    private val client: HttpClient,
    private val serverUrl: String,
) {
    private val memoryCache = mutableMapOf<String, ImageBitmap>()

    suspend fun getDrawable(name: String): ImageBitmap {
        memoryCache[name]?.let { return it }

        val image = client
            .get("$serverUrl/assets/drawable/$name.png")
            .body<ByteArray>()
            .decodeToImageBitmap()

        memoryCache[name] = image
        return image
    }
}