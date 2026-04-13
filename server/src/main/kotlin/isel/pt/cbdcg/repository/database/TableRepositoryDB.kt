package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.*
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.Tables
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object TableRepositoryDB : TableRepository {

    override fun createTable(name: Name, owner: User, participant: Participant): Table {
        transaction {
            Tables.insert {
                it[Tables.name] = name.string
                it[Tables.owner] = owner.id
                it[capacity] = 1u
            }
            Participants.insert {
                it[id] = participant.user.id
                it[userEmail] = participant.user.email.string
                it[lobbyName] = name.string
                it[role] = participant.role.name
            }
        }
        return findByName(name)!!
    }

    override fun findById(id: UInt): Table? {
        return transaction {
            Tables.selectAll().where { Tables.id eq id }
                .singleOrNull()
                ?.toTable()
        }
    }

    override fun findByName(name: Name): Table? {
        return transaction {
            Tables.selectAll().where { Tables.name eq name.string }
                .singleOrNull()
                ?.toTable()
        }
    }

    override fun getAllTables(): List<Table> {
        return transaction {
            Tables.selectAll().map { it.toTable() }
        }
    }

    override fun getAllParticipants(tableName: Name): List<Participant> {
        return transaction {
            Participants.selectAll().where { Participants.lobbyName eq tableName.string }
                .map { it.toParticipant() }
        }
    }

    override fun save(element: Table) {
        transaction {
            if (element.id == 0u) {
                Tables.insert {
                    it[name] = element.name.string
                    it[owner] = element.owner.id
                    it[capacity] = element.participants.size.toUInt()
                }
            } else {
                Tables.update({ Tables.id eq element.id }) {
                    it[name] = element.name.string
                    it[owner] = element.owner.id
                    it[capacity] = element.participants.size.toUInt()
                }
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

    override fun removeParticipant(table: Table, user: User): Table {
        transaction {
            Participants.deleteWhere { (lobbyName eq table.name.string) and (id eq user.id) }
            Tables.update({ Tables.id eq table.id }) {
                it[capacity] = (table.participants.size - 1).toUInt()
            }
        }
        return findById(table.id)!!
    }

    override fun updateParticipants(table: Table, participant: Participant): Table {
        transaction {
            val count = Participants.selectAll().where { (Participants.lobbyName eq table.name.string) and (Participants.id eq participant.user.id) }.count()
            if (count == 0L) {
                Participants.insert {
                    it[id] = participant.user.id
                    it[userEmail] = participant.user.email.string
                    it[lobbyName] = table.name.string
                    it[role] = participant.role.name
                }
            } else {
                Participants.update({ (Participants.lobbyName eq table.name.string) and (Participants.id eq participant.user.id) }) {
                    it[role] = participant.role.name
                }
            }
            Tables.update({ Tables.id eq table.id }) {
                it[capacity] = getAllParticipants(table.name).size.toUInt()
            }
        }
        return findById(table.id)!!
    }

    private fun ResultRow.toTable() = Table(
        id = this[Tables.id],
        name = Name(this[Tables.name]),
        owner = UserRepositoryDB.findById(this[Tables.owner])!!,
        participants = getAllParticipants(Name(this[Tables.name]))
    )

    private fun ResultRow.toParticipant() = Participant(
        user = UserRepositoryDB.findById(this[Participants.id])!!,
        role = this[Participants.role].toRole() ?: Role.SPECTATOR
    )

}