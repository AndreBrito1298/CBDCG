package isel.pt.cbdcg.repository.memory

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User
import isel.pt.cbdcg.repository.TableRepository


object TableRepositoryMem: TableRepository {

    /**
     * List of Game Tables registered.
     */
    val tables = mutableListOf<Table>()


    override fun getAllTables(): List<Table> {
        return tables
    }

    override fun createTable(name: Name, owner: User, participant: Participant): Table {

        val table = Table(tables.size.toUInt(), name, owner, listOf(participant))
        tables.add(table)

        return table
    }

    override fun findByName(name: Name): Table? {
        return tables.find{ it.name.string == name.string }
    }

    override fun removeParticipant(table: Table, user: User): Table {
        return table.copy(participants = table.participants.filter{ it.user == user })
    }

    override fun updateParticipants(table: Table, participant: Participant): Table {
        val list = table.participants.filter{ it.user == participant.user }
        return table.copy(participants = list.plus(participant))
    }

    // Generic Operations

    override fun findById(id: UInt): Table? {
        return tables.find{ it.id == id}
    }

    override fun save(element: Table) {
        tables.add(element)
    }

    override fun deleteById(id: UInt) {
        tables.removeIf{ it.id == id}
    }

    override fun clear() {
        tables.clear()
    }


}
