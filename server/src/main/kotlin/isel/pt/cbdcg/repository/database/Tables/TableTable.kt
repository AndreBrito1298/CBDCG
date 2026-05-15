package isel.pt.cbdcg.repository.database.Tables

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import isel.pt.cbdcg.domain.Name
import isel.pt.cbdcg.domain.Table
import isel.pt.cbdcg.repository.database.TableRepositoryDB.getAllParticipants
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

object Tables : IntIdTable("tables") {
    val name = varchar("name", MAX_NAME_LENGTH).uniqueIndex()
    val owner = integer("owner").references(Users.id)
    val capacity = integer("capacity")

}

class TablesDao(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TablesDao>(Tables)

    var name by Tables.name
    var owner by Tables.owner
    var capacity by Tables.capacity

    override fun toString(): String {
        return "Table(id=$id, name=$name, owner=$owner, capacity=$capacity)"
    }

    fun toTable() = Table(
        id = id.value.toUInt(),
        name = Name(name),
        owner = UsersDao.findById(owner)?.toUser()!!,
        participants = getAllParticipants(id.value.toUInt()),
    )
}
