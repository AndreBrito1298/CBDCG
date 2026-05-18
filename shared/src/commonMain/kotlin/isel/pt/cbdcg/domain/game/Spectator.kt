package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.domain.toUserDTO
import isel.pt.cbdcg.dto.SpectatorDTO
import isel.pt.cbdcg.dto.toUser

data class Spectator(
    val user: User
)

fun Spectator.toSpectatorDTO(): SpectatorDTO =
    SpectatorDTO(
        user = user.toUserDTO()
    )

fun SpectatorDTO.toSpectator(): Spectator = Spectator(user.toUser())