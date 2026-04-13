package isel.pt.cbdcg.configs

import org.jetbrains.exposed.sql.Table
import java.util.Collections.list

object BDConfig {
    const val DB_NAME = "cbdcg"
    const val DB_URL = "jdbc:postgresql://localhost:5432/$DB_NAME"
    const val DB_USER = "postgres"
    const val DB_PASSWORD = ""
}

private const val MAX_CONNECTIONS = 100
const val MAX_NAME_LENGTH = 20
private const val MAX_PASSWORD_LENGTH = 20
const val MAX_EMAIL_LENGTH = 7

