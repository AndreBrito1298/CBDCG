package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

interface ParticipantRepository {
    fun createParticipant(user: User, table: Table, role: Role):Participant

    fun userAvailability(user: User): Boolean

    fun deleteParticipant(user: User)

    fun clear()

}