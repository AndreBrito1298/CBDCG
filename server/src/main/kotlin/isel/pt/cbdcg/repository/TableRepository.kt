package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

interface TableRepository: Repository<Table> {

    suspend fun getAllTables(): List<Table>

    suspend fun createTable(name: Name, owner: User, participant: Participant): Table

    suspend fun findByName(name: Name): Table?

    suspend fun removeParticipant(table: Table, user: User): Table

    suspend fun updateParticipants(table: Table, participant: Participant): Table

   // fun getAllParticipants(tableName: Name): List<Participant>
    suspend fun getAllParticipants(tableId: UInt): List<Participant>

}