package isel.pt.cbdcg.domain.game

import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.dto.SpectatorDTO
import isel.pt.cbdcg.dto.toUserDTO

data class Spectator(
    val user: User
) {
    fun toSpectatorDTO(): SpectatorDTO =
        SpectatorDTO(
        user = user.toUserDTO()
        )
}
