package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Role
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

interface ParticipantRepository: Repository<Participant> {
    suspend fun createParticipant(user: User, table: Table, role: Role):Participant

    suspend fun userAvailability(user: User): Boolean

    suspend fun deleteParticipant(user: User)

}