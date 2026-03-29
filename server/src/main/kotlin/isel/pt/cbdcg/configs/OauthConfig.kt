package isel.pt.cbdcg.config

import io.ktor.http.*
import io.ktor.server.auth.*

object OAuthConfig {
    // Google OAuth settings
    val googleOAuthSettings = OAuthServerSettings.OAuth2ServerSettings(
        name = "google",
        authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
        accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
        requestMethod = HttpMethod.Post,
        clientId = System.getenv("GOOGLE_CLIENT_ID") ?: "",
        clientSecret = System.getenv("GOOGLE_CLIENT_SECRET") ?: "",
        defaultScopes = listOf(
            "https://www.googleapis.com/auth/userinfo.profile",
            "https://www.googleapis.com/auth/userinfo.email"
        )
    )

    const val CALLBACK_URL = "http://localhost:8080/auth/callback"
}