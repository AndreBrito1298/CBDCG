package isel.pt.cbdcg.repository.database

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

    override fun save(element: Table) {
        transaction {
            Tables.insert {
                it[name] = element.name.string
                it[owner] = element.owner
                it[capacity] = element.players.toUInt()
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
        players = this[Tables.capacity].toInt()
    )
}