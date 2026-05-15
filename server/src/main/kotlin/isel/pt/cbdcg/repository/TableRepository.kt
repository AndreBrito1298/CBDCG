package isel.pt.cbdcg.repository

import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Participant
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.domain.User

interface TableRepository: Repository<Table> {

    fun getAllTables(): List<Table>

    fun createTable(name: Name, owner: User, participant: Participant): Table

    fun findByName(name: Name): Table?

    fun removeParticipant(table: Table, user: User): Table

    fun updateParticipants(table: Table, participant: Participant): Table

   // fun getAllParticipants(tableName: Name): List<Participant>
   fun getAllParticipants(tableId: UInt): List<Participant>

}