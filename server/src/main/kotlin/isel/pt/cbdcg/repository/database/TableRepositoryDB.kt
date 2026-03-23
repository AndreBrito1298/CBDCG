package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.Repository

object TableRepositoryDB: Repository<Table> {
    override fun findById(id: UInt): Table? {
        TODO("Not yet implemented")
    }

    override fun save(element: Table) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: UInt) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}