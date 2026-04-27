package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.*
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.ParticipantsDao
import isel.pt.cbdcg.repository.database.Tables.Tables
import isel.pt.cbdcg.repository.database.Tables.TablesDao
import isel.pt.cbdcg.repository.database.Tables.UsersDao
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

object TableRepositoryDB : TableRepository {

    override fun createTable(name: Name, owner: User, participant: Participant): Table {
        transaction {
            val createdTable = TablesDao.new {
                this.name = name.string
                this.owner = owner.id.toInt()
                this.capacity = 1
            }

            ParticipantsDao.new(participant.user.id.toInt()) {
                userEmail = participant.user.email.string
                lobbyName = createdTable.name
                role = participant.role.name
            }
        }
        return findByName(name)!!
    }

    override fun findById(id: UInt): Table? {
        return transaction {
            TablesDao.findById(id.toInt())?.toTable()
        }
    }

    override fun findByName(name: Name): Table? {
        return transaction {
            TablesDao.find { Tables.name eq name.string }
                .singleOrNull()
                ?.toTable()
        }
    }

    override fun getAllTables(): List<Table> {
        return transaction {
            TablesDao.all().map { it.toTable() }
        }
    }

    override fun getAllParticipants(tableName: Name): List<Participant> {
        return transaction {
            ParticipantsDao.find { Participants.lobbyName eq tableName.string }
                .map { it.toParticipant() }
        }
    }

    override fun save(element: Table) {
        transaction {
            val existing = TablesDao.findById(element.id.toInt())
            if (existing == null) {
                TablesDao.new {
                    name = element.name.string
                    owner = element.owner.id.toInt()
                    capacity = element.participants.size
                }
            } else {
                existing.name = element.name.string
                existing.owner = element.owner.id.toInt()
                existing.capacity = element.participants.size
            }
        }
    }

    override fun deleteById(id: UInt) {
        transaction {
            TablesDao.findById(id.toInt())?.delete()
        }
    }

    override fun clear() {
        transaction {
            ParticipantsDao.all().forEach { it.delete() }
            TablesDao.all().forEach { it.delete() }
        }
    }

    override fun removeParticipant(table: Table, user: User): Table {
        transaction {
            ParticipantsDao.find { (Participants.lobbyName eq table.name.string) and (Participants.id eq user.id.toInt()) }
                .forEach { it.delete() }
            TablesDao.findById(table.id.toInt())?.capacity = (table.participants.size - 1)
        }
        return findById(table.id)!!
    }

    override fun updateParticipants(table: Table, participant: Participant): Table {
        transaction {
            val existing = ParticipantsDao.find {
                (Participants.lobbyName eq table.name.string) and (Participants.id eq participant.user.id.toInt())
            }.singleOrNull()
            if (existing == null) {
                ParticipantsDao.new(participant.user.id.toInt()) {
                    userEmail = participant.user.email.string
                    lobbyName = table.name.string
                    role = participant.role.name
                }
            } else {
                existing.role = participant.role.name
            }
            TablesDao.findById(table.id.toInt())?.capacity = getAllParticipants(table.name).size
        }
        return findById(table.id)!!
    }
}
