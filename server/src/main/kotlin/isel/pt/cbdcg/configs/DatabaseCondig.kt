package isel.pt.cbdcg.configs

import isel.pt.cbdcg.repository.database.Tables.AuthUsers
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.Tables
import isel.pt.cbdcg.repository.database.Tables.Users
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.util.Collections.list

fun dbInit(reset: Boolean = false) {
    Database.connect(
        "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )

    if(reset){
        transaction {
            SchemaUtils.drop(Participants)
            SchemaUtils.drop(AuthUsers)
            SchemaUtils.drop(Tables)
            SchemaUtils.drop(Users)

            SchemaUtils.create(Users)
            SchemaUtils.create(AuthUsers)
            SchemaUtils.create(Participants)
            SchemaUtils.create(Tables)
        }
    }
}
const val MAX_NAME_LENGTH = 20

