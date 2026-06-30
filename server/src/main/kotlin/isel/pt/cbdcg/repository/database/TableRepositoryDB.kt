package isel.pt.cbdcg.repository.database

import isel.pt.cbdcg.domain.*
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.TableRepository
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.ParticipantsDao
import isel.pt.cbdcg.repository.database.Tables.Tables
import isel.pt.cbdcg.repository.database.Tables.TablesDao
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

object TableRepositoryDB : TableRepository {

    override suspend fun createTable(name: Name, owner: User, participant: Participant): Table {
        suspendTransaction {
            val createdTable = TablesDao.new {
                this.name = name.string
                this.owner = owner.id.toInt()
                this.capacity = 1
            }
            ParticipantsDao.new(participant.user.id.toInt()) {
                userId = participant.user.id.toInt()
                lobbyId = createdTable.id.value
                role = participant.role.name
            }
        }
        return findByName(name)!!
    }

    override suspend fun findById(id: UInt): Table? {
        return suspendTransaction {
            TablesDao.findById(id.toInt())?.toTable()
        }
    }

    override suspend fun findByName(name: Name): Table? {
        return suspendTransaction {
            TablesDao.find { Tables.name eq name.string }
                .singleOrNull()
                ?.toTable()
        }
    }

    override suspend fun getAllTables(): List<Table> {
        return suspendTransaction {
            TablesDao.all().map { it.toTable() }
        }
    }


    override suspend fun getAllParticipants(tableId: UInt): List<Participant> {
        return suspendTransaction {
            ParticipantsDao.find { Participants.lobbyId eq tableId.toInt() }
                .map { it.toParticipant() }
        }
    }


    override suspend fun save(element: Table) {
        suspendTransaction {
            val table = TablesDao.findById(element.id.toInt())
                ?: TablesDao.new(element.id.toInt()) {
                    name = element.name.string
                    owner = element.owner.id.toInt()
                    capacity = element.participants.size
                }

            table.name = element.name.string
            table.owner = element.owner.id.toInt()
            table.capacity = element.participants.size

            val participantUserIds = element.participants.map { it.user.id.toInt() }.toSet()
            ParticipantsDao.find { Participants.lobbyId eq element.id.toInt() }
                .filter { it.userId !in participantUserIds }
                .forEach { it.delete() }

            element.participants.forEach { participant ->
                val existing = ParticipantsDao.find { Participants.userId eq participant.user.id.toInt() }
                    .singleOrNull()
                    ?: ParticipantsDao.new(participant.user.id.toInt()) {
                        userId = participant.user.id.toInt()
                        lobbyId = element.id.toInt()
                        role = participant.role.name
                    }

                existing.userId = participant.user.id.toInt()
                existing.lobbyId = element.id.toInt()
                existing.role = participant.role.name
            }
        }
    }

    override suspend fun deleteById(id: UInt) {
        suspendTransaction {
            ParticipantsDao.find { Participants.lobbyId eq id.toInt() }
                .forEach { it.delete() }
            TablesDao.findById(id.toInt())?.delete()
        }
    }

    override suspend fun clear() {
        suspendTransaction {
            ParticipantsDao.all().forEach { it.delete() }
            TablesDao.all().forEach { it.delete() }
        }
    }

    override suspend fun removeParticipant(table: Table, user: User): Table {
        suspendTransaction {
            ParticipantsDao.find { (Participants.lobbyId eq table.id.toInt()) and (Participants.userId eq user.id.toInt()) }
                .forEach { it.delete() }
            TablesDao.findById(table.id.toInt())?.capacity =
                ParticipantsDao.find { Participants.lobbyId eq table.id.toInt() }.toList().size
        }
        return findById(table.id)!!
    }

    override suspend fun updateParticipants(table: Table, participant: Participant): Table {
        suspendTransaction {
            val existing = ParticipantsDao.find {
                (Participants.lobbyId eq table.id.toInt()) and (Participants.userId eq participant.user.id.toInt())
            }.singleOrNull()
            if (existing == null) {
                ParticipantsDao.new(participant.user.id.toInt()) {
                    userId = participant.user.id.toInt()
                    lobbyId = table.id.toInt()
                    role = participant.role.name
                }
            } else {
                existing.role = participant.role.name
            }
            TablesDao.findById(table.id.toInt())?.capacity =
                ParticipantsDao.find { Participants.lobbyId eq table.id.toInt() }.toList().size
        }
        return findById(table.id)!!
    }
}
