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


    override suspend fun getAllTables(): List<Table> {
        return tables
    }

    override suspend fun createTable(name: Name, owner: User, participant: Participant): Table {

        val table = Table(tables.size.toUInt(), name, owner, listOf(participant))
        tables.add(table)

        return table
    }

    override suspend fun findByName(name: Name): Table? {
        return tables.find{ it.name.string == name.string }
    }

    override suspend fun removeParticipant(table: Table, user: User): Table {
        tables.removeIf{ it.id == table.id }
        val newTable = table.copy(participants = table.participants.filter{ it.user.id != user.id })
        tables.add(newTable)
        return newTable
    }

    override suspend fun updateParticipants(table: Table, participant: Participant): Table {
        tables.removeIf{ it.id == table.id }
        val list = table.participants.filter{ it.user.id != participant.user.id }
        val newTable = table.copy(participants = list.plus(participant))
        tables.add(newTable)
        return newTable
    }

    override suspend fun getAllParticipants(tableId: UInt): List<Participant> {
        return tables.find { it.id == tableId }?.participants ?: listOf()
    }

    // Generic Operations

    override suspend fun findById(id: UInt): Table? {
        return tables.find{ it.id == id}
    }

    override suspend fun save(element: Table) {
        tables.removeIf{ it.id == element.id }
        tables.add(element)
    }

    override suspend fun deleteById(id: UInt) {
        tables.removeIf{ it.id == id}
    }

    override suspend fun clear() {
        tables.clear()
    }


}
