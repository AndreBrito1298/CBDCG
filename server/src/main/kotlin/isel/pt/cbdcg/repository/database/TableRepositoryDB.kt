package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.configs.Tables
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.Repository
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object TableRepositoryDB: Repository<Table> {

    fun createTable(table: Table) = transaction {
        save(table)
        //ParticipantRepositoryDB.joinTable()
    }
    
    override fun findById(id: UInt): Table? {
        return transaction {
            Tables.selectAll().where { Tables.id eq id }
                .singleOrNull()
                ?.toTable()
        }
    }

    fun findByName(name: Name): Table? {
        return transaction {
            Tables.selectAll().where { Tables.name eq name.string }
                .singleOrNull()
                ?.toTable()
        }
    }

    fun addPlayerToTable(table: Table) {
        transaction {
            Tables.update({ Tables.id eq table.id }) {
                it[Tables.players] = table.participants+1.toUInt()
            }
        }
    }

    fun getAllTables(): List<Table> {
        return transaction {
            Tables.selectAll().map { it.toTable() }
        }
    }

    override fun save(element: Table) {
        transaction {
            Tables.insert {
                it[name] = element.name.string
                it[owner] = element.owner
                it[players] = element.participants
            }
        }
    }

    override fun deleteById(id: UInt) {
        transaction {
            Tables.deleteWhere { Tables.id eq id }
        }
    }

    override fun clear() {
        transaction {
            Tables.deleteAll()
        }
    }

    /**
     * Helper function to convert a ResultRow to a Table domain object.
     */
    private fun ResultRow.toTable() = Table(
        id = this[Tables.id],
        name = Name(this[Tables.name]),
        owner = this[Tables.owner],
        participants = this[Tables.players]
    )
}