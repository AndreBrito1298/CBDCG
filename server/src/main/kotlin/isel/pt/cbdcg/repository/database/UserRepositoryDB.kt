package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.Repository

object UserRepositoryDB: Repository<User> {
    override fun findById(id: UInt): User? {
        TODO("Not yet implemented")
    }

    override fun save(element: User) {
        TODO("Not yet implemented")
    }

    override fun deleteById(id: UInt) {
        TODO("Not yet implemented")
    }

    override fun clear() {
        TODO("Not yet implemented")
    }
}