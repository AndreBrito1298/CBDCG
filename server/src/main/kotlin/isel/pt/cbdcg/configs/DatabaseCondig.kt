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
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

fun dbInit(reset: Boolean = false) {
    Database.connect(
        "jdbc:postgresql://localhost:5432/postgres",
        driver = "org.postgresql.Driver",
        user = "postgres",
        password = "password"
    )

    if(reset){
        transaction {
            SchemaUtils.drop(BoardCharacterItems)
            SchemaUtils.drop(BoardCharacterModifiers)
            SchemaUtils.drop(BoardTiles)
            SchemaUtils.drop(GameSpectators)
            SchemaUtils.drop(GamePlayers)
            SchemaUtils.drop(Games)
            SchemaUtils.drop(Participants)
            SchemaUtils.drop(AuthUsers)
            SchemaUtils.drop(Tables)
            SchemaUtils.drop(Users)

            SchemaUtils.create(Users)
            SchemaUtils.create(AuthUsers)
            SchemaUtils.create(Tables)
            SchemaUtils.create(Participants)
            SchemaUtils.create(Games)
            SchemaUtils.create(GamePlayers)
            SchemaUtils.create(GameSpectators)
            SchemaUtils.create(BoardTiles)
            SchemaUtils.create(BoardCharacterItems)
            SchemaUtils.create(BoardCharacterModifiers)
        }
    }
}
const val MAX_NAME_LENGTH = 20

