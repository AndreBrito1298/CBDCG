package isel.pt.cbdcg.service

import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.game.Game
import isel.pt.cbdcg.domain.game.Player
import isel.pt.cbdcg.domain.verifyToken
import isel.pt.cbdcg.error.GameError
import isel.pt.cbdcg.error.TableError
import isel.pt.cbdcg.error.UserError
import isel.pt.cbdcg.repository.GameRepository
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.UserRepository
import isel.pt.cbdcg.webapi.websocket.EventsPublisher

class GameService(
    private val gameRepo: GameRepository,
    private val tableRepo: TableRepository,
    private val userRepo: UserRepository,
    private val events: EventsPublisher,
) {

    suspend fun createGame(tableId: UInt, userId: UInt, token: String): Result<Game> = runCatching {

        val user = userRepo.findById(userId)
            ?: throw UserError.IdNotFound()
        token.verifyToken(user)

        val table = tableRepo.findById(tableId)
            ?: throw TableError.TableDoesNotExist(tableId.toString())

        if(table.owner.id != userId)
            throw TableError.OwnerOnly()

        val players = table.participants
            .filter{ it.role == Role.PLAYER }
            .mapIndexed{ idx, participant -> Player(participant.user.id, idx.toUInt()) }

        if(players.size < 2)
            throw GameError.MinimumPlayersNeeded()

        val game = gameRepo.createGame(players)
        events.publishGameStarted(table, game)

        game
    }


}