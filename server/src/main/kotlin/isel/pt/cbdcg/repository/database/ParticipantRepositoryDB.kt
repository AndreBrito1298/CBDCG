package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.repository.Repository

object ParticipantRepositoryDB:Repository<Participant>  {
    override fun findById(id: UInt): Participant? {
        TODO("Not yet implemented")
    }

    override fun save(element: Participant) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: UInt) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}