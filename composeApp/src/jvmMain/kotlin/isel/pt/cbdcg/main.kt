package isel.pt.cbdcg

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.FileStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import java.io.File

fun main() = application {

    val httpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json{
                    ignoreUnknownKeys = true
                }
            )
        }
        install(WebSockets)
        install(HttpCache) {

            val baseDir = System.getProperty("user.home") ?: "."
            val cacheDir = File(baseDir, ".cbdcg/cache/http")

            publicStorage(FileStorage(cacheDir))
        }

        defaultRequest {
            headers.append(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
    }

    Window(
        onCloseRequest = {
            httpClient.close()
            exitApplication()
        },
        title = "cbdcg",
    ) {
        App(httpClient)
    }
}