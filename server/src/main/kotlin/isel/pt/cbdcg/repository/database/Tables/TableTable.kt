package isel.pt.cbdcg.repository.database.Tables

import com.android.identity.cbor.Uint
import isel.pt.cbdcg.configs.MAX_NAME_LENGTH
import org.jetbrains.exposed.sql.Table

object Tables : Table("tables") {
    val id = uinteger("id").autoIncrement()
    val name = varchar("name", MAX_NAME_LENGTH).uniqueIndex()
    val owner = uinteger("owner").references(Users.id)
    val capacity = uinteger("capacity")

    override val primaryKey = PrimaryKey(id)
}

data class TableDTO(
    val id: Uint,
    val name: String,
    val owner: Uint,
    val capacity: Uint
)