package isel.pt.cbdcg.configs

import isel.pt.cbdcg.repository.database.Tables.AuthUsers
import isel.pt.cbdcg.repository.database.Tables.Game.BoardCharacterItems
import isel.pt.cbdcg.repository.database.Tables.Game.BoardCharacterModifiers
import isel.pt.cbdcg.repository.database.Tables.Game.BoardTiles
import isel.pt.cbdcg.repository.database.Tables.Game.GamePlayers
import isel.pt.cbdcg.repository.database.Tables.Game.GameSpectators
import isel.pt.cbdcg.repository.database.Tables.Game.Games
import isel.pt.cbdcg.repository.database.Tables.Participants
import isel.pt.cbdcg.repository.database.Tables.Tables
import isel.pt.cbdcg.repository.database.Tables.Users
import org.jetbrains.exposed.v1.core.Schema
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.vendors.currentDialectMetadata

private val publicSchema = Schema("public")

private fun dropApplicationTablesCascade() {
    SchemaUtils.dropSchema(publicSchema, cascade = true)
    SchemaUtils.createSchema(publicSchema)
    currentDialectMetadata.resetCaches()
}

fun dbInit(reset: Boolean = false) {
    Database.connect(
        "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )

    if(reset){
        transaction {
            dropApplicationTablesCascade()

            SchemaUtils.create(
                Users,
                AuthUsers,
                Tables,
                Participants,
                Games,
                GamePlayers,
                GameSpectators,
                BoardTiles,
                BoardCharacterItems,
                BoardCharacterModifiers,
            )
        }
    }
}
const val MAX_NAME_LENGTH = 20
