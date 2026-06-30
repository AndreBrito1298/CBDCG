package isel.pt.cbdcg

import io.ktor.client.HttpClient
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.OAuthServerSettings
import io.ktor.server.auth.oauth
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.timeout
import io.ktor.util.collections.ConcurrentMap
import isel.pt.cbdcg.error.BoardError
import isel.pt.cbdcg.error.Error
import isel.pt.cbdcg.error.GameError
import isel.pt.cbdcg.error.ParticipantError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import io.ktor.http.HttpHeaders
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.plugins.cors.routing.CORS
import isel.pt.cbdcg.error.BattleError
import isel.pt.cbdcg.error.CardError
import isel.pt.cbdcg.error.CharacterError

fun Application.installPlugins(httpclient: HttpClient) {

    install(ConditionalHeaders) {}

    install(WebSockets) {
        pingPeriod = 15.seconds
        timeout = 15.seconds
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }

    install(ContentNegotiation) {
        json(
            Json{
                ignoreUnknownKeys = true
            }
        )
    }

    install(CORS) {
        allowHost("localhost:8081") // Aceita qualquer endereço ou introduzir um token no URL

        // Aceita tudo
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowMethod(HttpMethod.Get)
        allowMethod(HttpMethod.Post)
    }

    val redirects = ConcurrentMap<String, String>()
    install(Authentication) {
        oauth("auth-oauth-google") {
            // Configure oauth authentication
            urlProvider = { "http://localhost:8080/callback" }
            providerLookup = {
                OAuthServerSettings.OAuth2ServerSettings(
                    name = "google",
                    authorizeUrl = "https://accounts.google.com/o/oauth2/auth",
                    accessTokenUrl = "https://accounts.google.com/o/oauth2/token",
                    requestMethod = HttpMethod.Post,
                    clientId = System.getenv("GOOGLE_CLIENT_ID"),
                    clientSecret = System.getenv("GOOGLE_CLIENT_SECRET"),
                    defaultScopes = listOf("https://www.googleapis.com/auth/userinfo.profile"),
                    extraAuthParameters = listOf("access_type" to "offline"),
                    onStateCreated = { call, state ->
                        //saves new state with redirect url value
                        call.request.queryParameters["redirectUrl"]?.let {
                            redirects[state] = it
                        }
                    }
                )
            }
            //+é suposto ter fallback mas propriedade nao está a ser encontrada
            /*
            fallback = { cause ->
                            if (cause is OAuth2RedirectError) {
                                respondRedirect("/login-after-fallback")
                            } else {
                                respond(HttpStatusCode.Forbidden, cause.message)
                            }
                        }
             */

            client = httpclient
        }
    }

    install(StatusPages) {

        exception<IllegalArgumentException>{ call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid format.")
        }

        exception<BadRequestException>{ call, cause ->
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Invalid request body.")
        }

        exception<Error>{ call, cause ->
            val (status, message) = cause.toHttpResponse()
            call.respond(status, message)
        }

        exception<Throwable> { call, _ ->
            call.respond(HttpStatusCode.InternalServerError, "Internal server error.")
        }
    }
}

fun Error.toHttpResponse(): Pair<HttpStatusCode, String>{

    val message = (message ?: desc)

    val code = when (this) {

        is UserError.DuplicateEmail -> HttpStatusCode.Conflict
        is UserError.EmailNotFound -> HttpStatusCode.NotFound
        is UserError.PasswordMismatch -> HttpStatusCode.Unauthorized
        is UserError.OAuthError -> TODO()
        is UserError.AlreadyLoggedIn -> HttpStatusCode.Conflict
        is UserError.TokenMismatch -> HttpStatusCode.Unauthorized
        is UserError.TokenNotFound -> HttpStatusCode.NotFound
        is UserError.IdNotFound -> HttpStatusCode.NotFound
        is UserError.NotLoggedIn -> HttpStatusCode.Unauthorized

        is TableError.DuplicateName -> HttpStatusCode.Conflict
        is TableError.UserUnavailable -> HttpStatusCode.Conflict
        is TableError.UserNotFound -> HttpStatusCode.NotFound
        is TableError.TableDoesNotExist -> HttpStatusCode.NotFound
        is TableError.OwnerOnly -> HttpStatusCode.Forbidden
        is TableError.MinimumPlayersNeeded -> HttpStatusCode.Conflict
        is TableError.EveryPlayerReady -> HttpStatusCode.Conflict

        is ParticipantError.ParticipantEmailNotFound -> HttpStatusCode.NotFound
        is ParticipantError.ParticipantIdNotFound -> HttpStatusCode.NotFound
        is ParticipantError.UserNotOnTable -> HttpStatusCode.NotFound

        is BoardError.PositionTaken -> HttpStatusCode.Conflict
        is BoardError.TileConnectionMismatch -> HttpStatusCode.Conflict
        is BoardError.CharacterLimitReached -> HttpStatusCode.Conflict
        is BoardError.EmptyTile -> HttpStatusCode.NotFound
        is BoardError.ApplyEffectOnYourCharacter -> HttpStatusCode.BadRequest
        is BoardError.TileNotFound -> HttpStatusCode.NotFound
        is BoardError.TileOccupied -> HttpStatusCode.Conflict
        is BoardError.NoTargetFound -> HttpStatusCode.NotFound
        is BoardError.EffectInCooldown -> HttpStatusCode.Conflict

        is GameError.InvalidFormat -> HttpStatusCode.BadRequest
        is GameError.NotYourTurn -> HttpStatusCode.Forbidden
        is GameError.GameNotFound -> HttpStatusCode.NotFound
        is GameError.PlayerNotFound -> HttpStatusCode.NotFound
        is GameError.DungeonTurnZeroRule -> HttpStatusCode.Conflict
        is GameError.MustPlaceTile -> HttpStatusCode.Conflict
        is GameError.NoActiveCharacters -> HttpStatusCode.Conflict
        is GameError.CharacterMovementRestriction -> HttpStatusCode.Conflict
        is GameError.EmptyDeck -> HttpStatusCode.Conflict
        is GameError.EffectNotFound -> HttpStatusCode.NotFound
        is GameError.CharacterPlacementRestriction -> HttpStatusCode.Conflict
        is GameError.EquipItemRestriction -> HttpStatusCode.Conflict
        is GameError.TilePlacementRestriction -> HttpStatusCode.Conflict
        is GameError.BattleNotConcluded -> HttpStatusCode.Conflict
        is GameError.NoBattleOngoing -> HttpStatusCode.NotFound
        is GameError.MustSelectATarget -> HttpStatusCode.BadRequest
        is GameError.MoveToBattleRestriction -> HttpStatusCode.Conflict
        is GameError.ItemGradeTooHigh -> HttpStatusCode.Conflict
        is GameError.ReplaceYourCharacter -> HttpStatusCode.Conflict

        is CardError.InvalidCardFormat -> HttpStatusCode.BadRequest
        
        is CharacterError.ItemCapacityLimit -> HttpStatusCode.Conflict
        is CharacterError.ItemDoesNotExist -> HttpStatusCode.NotFound
        is CharacterError.CharacterDoesNotExist -> HttpStatusCode.NotFound

        is BattleError.CharacterNotFound -> HttpStatusCode.NotFound
        is BattleError.InvalidAction -> HttpStatusCode.BadRequest
        is BattleError.ActionAlreadyQueued -> HttpStatusCode.Conflict
        is BattleError.ActionNotQueued -> HttpStatusCode.NotFound
        is BattleError.CantLeaveBattle -> HttpStatusCode.Conflict
        is BattleError.InvalidPhase -> HttpStatusCode.BadRequest
    }

    return code to message
}